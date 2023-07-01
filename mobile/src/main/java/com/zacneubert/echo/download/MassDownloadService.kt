package com.zacneubert.echo.download

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import android.util.Log
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Episode_
import com.zacneubert.echo.utils.WEEK
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.lang.Exception
import java.util.*


class MassDownloadService : Service() {
    companion object {
        const val EPISODES_KEY: String = "EPISODES_KEY"
        const val CHANNEL_ID: String = "Echo Downloading Episodes"

        const val BROADCAST_ACTION: String = "DOWNLOAD_PROGRESS"
        const val BROADCAST_PROGRESS_EPISODE_ID: String = "EPISODE_ID"
        const val BROADCAST_PROGRESS_PERCENT: String = "PROGRESS_PERCENT"

        const val NOTIFICATION_ID = 84

        fun ignitionIntentWIds(context: Context, episodes: List<Long>): Intent {
            val intent = Intent(context, MassDownloadService::class.java)
            intent.putStringArrayListExtra(EPISODES_KEY, episodes.map { eId -> "" + eId } as ArrayList<String>?)
            return intent
        }

        fun ignitionIntent(context: Context, episodes: List<Episode>): Intent {
            val intent = Intent(context, MassDownloadService::class.java)
            intent.putStringArrayListExtra(EPISODES_KEY, episodes.map { e -> "" + e.id } as ArrayList<String>?)
            return intent
        }

    }

    var progress: Double = 0.0
    var currentEpisodeId: Long = 0L

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { // Why does this happen?
            return START_NOT_STICKY
        }


        startForeground(NOTIFICATION_ID, buildNotification())

        registerReceiver()

        var episodeIds = intent.extras
                ?.getStringArrayList(EPISODES_KEY)
                ?.map { e_id -> e_id.toLong() }
        episodeIds?.let {ids ->
            downloadFiles(ids)
        }

        return START_STICKY
    }

    var receiver: BroadcastReceiver? = null
    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BROADCAST_ACTION)

        receiver = DownloadProgressReceiver({ episodeId: Long, percent: Double ->
            if (episodeId == this.currentEpisodeId) {
                progress = percent
                startForeground(NOTIFICATION_ID, buildNotification())
            }
        })

        this.registerReceiver(receiver, DownloadProgressReceiver.getIntentFilter())
    }

    private fun unregisterReceiver() {
        this.unregisterReceiver(receiver)
    }

    private fun broadcastDownloadProgress(context: Context, episode_id: Long, readBytes: Long, totalBytes: Long) {
        val intent = Intent(BROADCAST_ACTION)
        val percent = readBytes.toDouble() / totalBytes.toDouble()
        intent.putExtra(BROADCAST_PROGRESS_PERCENT, percent)
        intent.putExtra(BROADCAST_PROGRESS_EPISODE_ID, episode_id)
        context.sendBroadcast(intent)
    }

    private fun downloadFiles(episode_ids: List<Long>) {
        this@MassDownloadService.deleteOldEpisodes()

        doAsync {
            val application = (this@MassDownloadService.application as EchoApplication)
            val updatedEpisodes = HashSet<Episode>()
            episode_ids.forEach {
                this@MassDownloadService.currentEpisodeId = it
                this@MassDownloadService.progress = 0.0

                val episode = application.episodeBox()!!.get(it)
                val episodeFile = episode.getFile(this@MassDownloadService)
                if(episode.streamingUrl.isNotEmpty()) {
                    try {
                        downloadFile(episode.streamingUrl, episodeFile, this@MassDownloadService::notifyProgress)
                        episode.downloadDate = Date()
                        updatedEpisodes.add(episode)
                    } catch (e : Exception) {
                        Log.e("Download", "Could not download due to: " + e.message)
                    }
                }
            }

            uiThread {
                EchoApplication.instance(this@MassDownloadService).episodeBox().put(updatedEpisodes)
                this@MassDownloadService.stopSelf()
            }
        }
    }

    private fun deleteOldEpisodes() {
        val old = WEEK * 4L
        val reallyOld = WEEK * 6L

        val oldDate = Date(Date().time - old)
        val reallyOldDate = Date(Date().time - reallyOld)

        val episodeBox = EchoApplication.instance(this).episodeBox()

        val orphanedEpisodes = episodeBox.query()
                .isNull(Episode_.podcastId)
                .build()

        orphanedEpisodes.forEach {
            episodeBox.remove(it.id)
        }

        val updatedEpisodes = HashSet<Episode>()

        val oldPlayedEpisodes = episodeBox.query()
                .notNull(Episode_.downloadDate)
                .and().equal(Episode_.played, true)
                .and().less(Episode_.downloadDate, oldDate)
                .build()

        oldPlayedEpisodes.forEach {
            try {
                it.deleteFile(this)
                updatedEpisodes.add(it)
            } catch (e: Exception) {
                // don't fail just because you can't delete something
                var i = 1
                i++
            }
        }

        val reallyOldUnplayedEpisodes = episodeBox.query()
                .notNull(Episode_.downloadDate)
                .equal(Episode_.played, false)
                .and().less(Episode_.downloadDate, reallyOldDate)
                .build()

        reallyOldUnplayedEpisodes.forEach {
            try {
                it.deleteFile(this)
                updatedEpisodes.add(it)
            } catch (e: Exception) {
                // don't fail just because you can't delete something
                var i = 1
                i++
            }
        }

        episodeBox.put(updatedEpisodes)
    }

    private fun notifyProgress(readBytes: Long, totalBytes: Long) {
        broadcastDownloadProgress(
                this@MassDownloadService,
                this@MassDownloadService.currentEpisodeId,
                readBytes,
                totalBytes)
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
        val s = "[${"#".repeat(num)}${"--".repeat(max - num)}]"

        return if (currentEpisodeId != 0L) {
            val currentEpisode = (this.application as EchoApplication)
                    .episodeBox()!!
                    .get(currentEpisodeId)

            notificationBuilder!!
                    .setContentTitle("Downloading " + currentEpisode.title)
                    .setContentText(s)
                    .build()
        } else {
            notificationBuilder!!
                    .setContentTitle("Setting up download")
                    .setContentText(s)
                    .build()
        }
    }

    private fun repostNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }
}
