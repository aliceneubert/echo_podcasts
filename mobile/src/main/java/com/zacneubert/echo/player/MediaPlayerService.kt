package com.zacneubert.echo.player

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.view.KeyEvent
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode

class MediaPlayerService : Service() {
    companion object {
        val EPISODE_KEY: String = "EPISODE_KEY"
        val CHANNEL_ID: String = "Media Player Notification Channel"
        val NOTIFICATION_ID: Int = 6

        var mediaPlayerService: MediaPlayerService? = null
            private set

        fun ignitionIntent(context: Context, episode: Episode): Intent {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.putExtra(EPISODE_KEY, episode)
            return intent
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    lateinit var episode: Episode
    var mediaPlayer: MediaPlayer = MediaPlayer()
        private set

    var mediaStateChangeListeners: MutableList<OnMediaStateChangeListener> = mutableListOf()
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        episode = intent.extras.get(EPISODE_KEY) as Episode
        mediaPlayerService = this

        startForeground(NOTIFICATION_ID, buildNotification(true))

        mediaPlayer.setDataSource(this, episode.getUri())
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener {
                this.stopSelf()
            }
            refreshStateListeners()
        }
        mediaPlayer.prepareAsync()

        return START_STICKY
    }

    private fun buildNotificationAction(keyCode: Int) : PendingIntent {
        val mediaIntent = Intent(this, MediaIntentReceiver::class.java)
        mediaIntent.putExtra(MediaIntentReceiver.MEDIA_INTENT_KEY_CODE, keyCode)
        return PendingIntent.getBroadcast(this, keyCode, mediaIntent, 0)
    }

    private fun buildNotification(isPlaying: Boolean = mediaPlayer.isPlaying): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val intent = MainActivity.ignitionIntent(this, MainActivity.MainFragmentChoice.NOW_PLAYING)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentText(episode.podcast.title)
                .setContentTitle(episode.title)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD))
                .addAction(if(isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp, "Pause", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD))
                .setPriority(PRIORITY_MIN)
                .setVibrate(longArrayOf(0))
                .setContentIntent(pendingIntent)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle())
                .build()
    }

    fun repostNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        mediaPlayerService = null
    }

    private fun refreshStateListeners() {
        mediaStateChangeListeners.forEach {
            it.onMediaStateChange(mediaPlayer, episode)
        }
    }

    fun togglePlaying() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
        } else {
            mediaPlayer.start()
        }
        refreshStateListeners()
        repostNotification()
    }

    fun skipForward() {
        val seconds = 30
        val skipTo = Math.min(mediaPlayer.currentPosition + seconds * 1000, mediaPlayer.duration)
        mediaPlayer.seekTo(skipTo)
        refreshStateListeners()
    }

    fun skipBackward() {
        val seconds = 30
        val skipTo = Math.max(mediaPlayer.currentPosition - seconds * 1000, 0)
        mediaPlayer.seekTo(skipTo)
        refreshStateListeners()
    }
}