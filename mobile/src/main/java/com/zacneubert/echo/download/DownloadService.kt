@file:Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.zacneubert.echo.download

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Episode_
import com.zacneubert.echo.models.Podcast
import com.zacneubert.echo.player.MediaIntentReceiver
import com.zacneubert.echo.player.MediaPlayerService
import org.jetbrains.anko.doAsync


class DownloadService : Service() {
    companion object {
        const val EPISODE_KEY: String = "EPISODE_KEY"
        const val AUTOPLAY_KEY: String = "AUTOPLAY_KEY"
        const val PLAYLIST_KEY: String = "PLAYLIST_KEY"
        const val PODCAST_KEY: String = "PODCAST_KEY"
        const val CHANNEL_ID: String = "Echo Downloading Episodes"

        const val BROADCAST_ACTION: String = "DOWNLOAD_PROGRESS"
        const val BROADCAST_EPISODE_ID: String = "EPISODE_ID"
        const val BROADCAST_PROGRESS_PERCENT: String = "PROGRESS_PERCENT"

        fun ignitionIntent(context: Context, episode: Episode, autoplay: Boolean = false): Intent {
            val intent = Intent(context, DownloadService::class.java)
            intent.putExtra(EPISODE_KEY, episode)
            intent.putExtra(AUTOPLAY_KEY, autoplay)
            return intent
        }

    }

    lateinit var episode: Episode
    lateinit var podcast: Podcast

    var notificationId: Int = 0
    var progress: Double = 0.0

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { // Why does this happen?
            return START_NOT_STICKY
        }

        episode = intent.extras?.get(EPISODE_KEY) as Episode

        val application = (application as EchoApplication)
        episode = application.episodeBox()!!.find(Episode_.__ID_PROPERTY, episode.id).first()
        podcast = episode.podcast.target

        notificationId = (podcast.id * 7 * episode.id * 11).toInt() // just a quick hash

        startForeground(notificationId, buildNotification())

        registerReceiver()

        val autoplay = intent.extras?.getBoolean(AUTOPLAY_KEY, false) ?: false
        downloadFile(this, autoplay)

        return START_STICKY
    }

    var receiver: BroadcastReceiver? = null
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION)

        receiver = DownloadProgressReceiver { episodeId: Long, percent: Double ->
            if (episodeId == episode.id) {
                progress = percent
                startForeground(notificationId, buildNotification())
            }
        }

        this.registerReceiver(receiver, DownloadProgressReceiver.getIntentFilter())
    }

    private fun unregisterReceiver() {
        this.unregisterReceiver(receiver)
    }

    private fun broadcastDownloadProgress(context: Context, readBytes: Long, totalBytes: Long) {
        val intent = Intent(BROADCAST_ACTION)
        val percent = readBytes.toDouble() / totalBytes.toDouble()
        intent.putExtra(BROADCAST_PROGRESS_PERCENT, percent)
        intent.putExtra(BROADCAST_EPISODE_ID, episode.id)
        context.sendBroadcast(intent)
    }


    private fun downloadFile(context: Context, autoplay: Boolean) {
        doAsync {
            downloadFile(episode.streamingUrl,
                    episode.getFile(this@DownloadService),
                    { readBytes: Long, totalBytes: Long -> broadcastDownloadProgress(context, readBytes, totalBytes) },
                    { autoplay(autoplay) }
            )

            this@DownloadService.stopSelf()
        }
    }

    private fun autoplay(shouldAutoplay: Boolean) {
        if (shouldAutoplay) {
            this.sendBroadcast(MediaIntentReceiver.intent(this, KeyEvent.KEYCODE_REFRESH))
        }
    }


    private fun buildNotificationChannel() {
        val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
        notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
        notificationChannel.enableVibration(false)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun buildSimplifiedNotification(): Notification {
        buildNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentTitle("Setting up download...")
                .setPriority(PRIORITY_MAX)
                .build()
    }

    var notificationBuilder: NotificationCompat.Builder? = null
    private fun buildNotification(): Notification {
        buildNotificationChannel()

        val openAppIntent = MainActivity.ignitionIntent(this, MainActivity.MainFragmentChoice.NOW_PLAYING)
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)


        if (notificationBuilder == null) {
            notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.echo_logo_placeholder)
                    .setPriority(PRIORITY_MAX)
                    .setContentIntent(openAppPendingIntent)
                    .setOngoing(true)
        }

        val percent = (progress * 100).toInt()
        val mod = 3
        val max = 100 / mod
        val num = percent / mod
        val s = "[${"#".repeat(num)}${"--".repeat(max-num)}]"

        return notificationBuilder!!
                .setContentTitle("Downloading " + episode.title)
                .setContentText(s)
                .build()
    }

    private fun repostNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }
}
