package com.zacneubert.echo.download

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import androidx.core.content.ContextCompat
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.download.DownloadService.Companion.CHANNEL_ID
import com.zacneubert.echo.models.Podcast
import org.jetbrains.anko.doAsync
import java.util.*


class MassDownloadSetupService : Service() {
    companion object {
        const val NOTIFICATION_ID = 77

        fun ignitionIntent(context: Context): Intent {
            val intent = Intent(context, MassDownloadSetupService::class.java)
            return intent
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) { // Why does this happen?
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildSimplifiedNotification())

        val application = this.application as EchoApplication
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        doAsync {
            val undownloadedEpisodeIds = mutableListOf<Long>()

            val total = application.podcastBox()!!.all.size
            var i = 0

            application.podcastBox()!!.all.forEach {
                i += 1
                notificationManager.notify(NOTIFICATION_ID, buildNotification(it, i, total))

                undownloadedEpisodeIds.addAll(
                        it.getRefreshedOrCurrentEpisodeList(application)
                                .filter { e -> !e.getFile(this@MassDownloadSetupService).exists() }
                                .map { e -> e.id }
                )
            }

            val allTopThreeIds = application.podcastBox().all
                    .map { p -> p.topNChronological(3) }
                    .flatten()
                    .map { e -> e.id }

            val undownloadedTopThrees = undownloadedEpisodeIds
                    .filter { eId -> allTopThreeIds.contains(eId) }

            ContextCompat.startForegroundService(
                    this@MassDownloadSetupService,
                    MassDownloadService.ignitionIntentWIds(
                            this@MassDownloadSetupService,
                            undownloadedTopThrees)
            )

            if(undownloadedTopThrees.isNotEmpty()) {
                val notificationManager = getSystemService(NotificationManager::class.java)
                notificationManager.notify(102, buildCompleteNotification(undownloadedTopThrees))
            }

            this@MassDownloadSetupService.stopSelf()
        }

        return START_STICKY
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

    private fun buildCompleteNotification(newEpisodeIds : List<Long>) : Notification {
        buildNotificationChannel()

        val episodeTitles = EchoApplication.instance(this).episodeBox().get(newEpisodeIds).map { e -> e.title }

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentTitle("New Episodes Available")
                .setContentText(episodeTitles.joinToString(limit = 3))
                .setPriority(PRIORITY_MAX)
                .build()
    }

    var notificationBuilder: NotificationCompat.Builder? = null
    private fun buildNotification(podcast: Podcast, i: Int, total: Int): Notification {
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

        return notificationBuilder!!
                .setContentTitle("Scanning ${podcast.title}...")
                .setContentText("$i/$total")
                .build()
    }
}
