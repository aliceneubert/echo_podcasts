package com.zacneubert.echo.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.*
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MIN
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.helpers.LegacyPodcastProvider
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast
import java.io.File
import java.util.*


class MediaPlayerService : MediaBrowserServiceCompat(), OnMediaStateChangeListener, AudioManager.OnAudioFocusChangeListener {
    companion object {
        const val EPISODE_KEY: String = "EPISODE_KEY"
        const val CHANNEL_ID: String = "Media Player Notification Channel"
        const val NOTIFICATION_ID: Int = 6
        const val UPDATE_FREQUENCY: Long = 500
        const val MEDIA_SESSION_TOKEN: String = "MediaPlayerService"

        const val MEDIA_ROOT_ID: String = "ECHO_MEDIA_ROOT"

        var mediaPlayerService: MediaPlayerService? = null
            private set

        fun ignitionIntent(context: Context, episode: Episode): Intent {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.putExtra(EPISODE_KEY, episode)
            return intent
        }
    }

    lateinit var episode: Episode
    var mediaPlayer: MediaPlayer = MediaPlayer()
        private set

    var mediaStateChangeListeners: MutableList<OnMediaStateChangeListener> = mutableListOf()
    var playbackState: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()
    var refreshTimer: Timer = Timer()

    private var mSession: MediaSessionCompat? = null
    private var mAudioManager: AudioManager? = null
    private var mMediaCallback: MediaSessionCallback = MediaSessionCallback()

    private var playerPrepared: Boolean = false

    override fun onCreate() {
        super.onCreate()

        mAudioManager = this.getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager?

        mSession = MediaSessionCompat(this, MEDIA_SESSION_TOKEN)
        sessionToken = mSession!!.sessionToken
        mSession!!.setCallback(mMediaCallback)
        mSession!!.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
        mSession!!.setPlaybackState(playbackState.setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE).build())

        val context = applicationContext
        val intent = Intent(context, MainActivity::class.java)
        val pi = PendingIntent.getActivity(context, 99 /*request code*/, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        mSession!!.setSessionActivity(pi)

        mSession!!.isActive = true
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        episode = intent.extras.get(EPISODE_KEY) as Episode
        mediaPlayerService = this

        mSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, episode.podcast.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.podcast.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, episode.title)
                        .build()
        )

        startForeground(NOTIFICATION_ID, buildNotification(true))

        initializeMediaPlayer()
        mediaPlayer.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
                initializeMediaPlayer()
                return true
            }
        })

        refreshTimer.schedule(object : TimerTask() {
            override fun run() {
                refreshStateListeners()
            }
        }, UPDATE_FREQUENCY, UPDATE_FREQUENCY)

        return START_STICKY
    }

    private fun initializeMediaPlayer() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        }

        playerPrepared = false
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(this, episode.getUri())
        mediaPlayer.setOnPreparedListener {
            playerPrepared = true
            start()
            seekTo(getStopTime())
            refreshStateListeners()
        }
        mediaPlayer.prepareAsync()
    }

    private fun buildNotificationAction(keyCode: Int): PendingIntent {
        val mediaIntent = Intent(this, MediaIntentReceiver::class.java)
        mediaIntent.putExtra(MediaIntentReceiver.MEDIA_INTENT_KEY_CODE, keyCode)
        return PendingIntent.getBroadcast(this, keyCode, mediaIntent, 0)
    }

    private fun buildNotification(isPlaying: Boolean = mediaPlayer.isPlaying): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val openAppIntent = MainActivity.ignitionIntent(this, MainActivity.MainFragmentChoice.NOW_PLAYING)
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentText(episode.podcast.title)
                .setContentTitle(episode.title)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD))
                .addAction(if (isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp, "Pause", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD))
                .setPriority(PRIORITY_MIN)
                .setVibrate(longArrayOf(0))
                .setContentIntent(openAppPendingIntent)
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle())
                .build()
    }

    private fun repostNotification() {
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
        mSession!!.release()
    }

    fun removeStateListener(onMediaStateChangeListener: OnMediaStateChangeListener) {
        if (mediaStateChangeListeners.contains(onMediaStateChangeListener)) {
            mediaStateChangeListeners.remove(onMediaStateChangeListener)
        }
    }

    fun addStateListener(onMediaStateChangeListener: OnMediaStateChangeListener) {
        if (!mediaStateChangeListeners.contains(onMediaStateChangeListener)) {
            mediaStateChangeListeners.add(onMediaStateChangeListener)
        }
    }

    private fun refreshStateListeners() {
        mediaStateChangeListeners.forEach {
            it.onMediaStateChange(mediaPlayer, episode)
        }
    }

    private fun audiofocusRequest(): AudioFocusRequest {
        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build()
    }

    fun saveStopTime() {
        val stopTime = mediaPlayer.currentPosition
        if(stopTime > 10000) { // Never save less than 10 seconds in, to avoid overwriting better save times
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(episode.getStopTimeKey(), stopTime).apply()
        }
    }

    fun getStopTime(): Int {
        val time = PreferenceManager.getDefaultSharedPreferences(this).getInt(episode.getStopTimeKey(), 0)
        return time
    }

    fun pause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            saveStopTime()
        }
        this.onMediaStateChange(mediaPlayer, episode)
        refreshStateListeners()
        repostNotification()
    }

    fun start() {
        mAudioManager!!.requestAudioFocus(audiofocusRequest())
        if (!mediaPlayer.isPlaying) {
            mediaPlayer.start()
        }
        this.onMediaStateChange(mediaPlayer, episode)
        refreshStateListeners()
        repostNotification()
    }

    fun togglePlaying() {
        if (mediaPlayer.isPlaying) {
            pause()
        } else {
            start()
        }
    }

    fun seekTo(to: Int) {
        mediaPlayer.seekTo(to)
        this.onMediaStateChange(mediaPlayer, episode)
    }

    fun skipForward() {
        val seconds = 30
        val skipTo = Math.min(mediaPlayer.currentPosition + seconds * 1000, mediaPlayer.duration)
        mediaPlayer.seekTo(skipTo)
        this.onMediaStateChange(mediaPlayer, episode)
        refreshStateListeners()
    }

    fun skipBackward() {
        val seconds = 30
        val skipTo = Math.max(mediaPlayer.currentPosition - seconds * 1000, 0)
        mediaPlayer.seekTo(skipTo)
        this.onMediaStateChange(mediaPlayer, episode)
        refreshStateListeners()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        val mediaItems = mutableListOf<MediaItem>()
        if (parentId == MEDIA_ROOT_ID) {
            val podcasts = LegacyPodcastProvider.getPodcastDirectory()
                    .listFiles()
                    .filter { f -> f.isDirectory }
                    .map { f -> Podcast(f) }
                    .sortedBy { p -> p.folder.lastModified() }
                    .reversed()
            podcasts.forEach {
                mediaItems.add(MediaItem(MediaDescriptionCompat.Builder()
                        .setTitle(it.title)
                        .setDescription(it.description)
                        .setMediaId(it.folder.absolutePath)
                        .build(), MediaItem.FLAG_BROWSABLE))
            }
        } else {
            val podcast = Podcast(File(parentId))
            podcast.episodes
                    .sortedBy { e -> e.file.lastModified() }
                    .reversed()
                    .forEach {
                        mediaItems.add(MediaItem(MediaDescriptionCompat.Builder()
                                .setTitle(it.title)
                                .setMediaUri(it.getUri())
                                .setMediaId(it.file.absolutePath)
                                .build(), MediaItem.FLAG_PLAYABLE))
                    }
        }
        result.sendResult(mediaItems)
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            start()
        }

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) {
            seekTo(position.toInt())
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            val episodeFile = File(mediaId)
            val podcast = Podcast(episodeFile.parentFile)
            val episode = Episode(podcast, episodeFile)
            applicationContext.startService(MediaPlayerService.ignitionIntent(applicationContext, episode))
        }

        override fun onPause() {
            pause()
        }

        override fun onStop() {
            mediaPlayer.stop()
        }

        override fun onSkipToNext() {
            skipForward()
        }

        override fun onSkipToPrevious() {
            skipBackward()
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {}

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {}
    }

    override fun onMediaStateChange(mediaPlayer: MediaPlayer, episode: Episode) {
        var builder = playbackState.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        builder = if (mediaPlayer.isPlaying) {
            builder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.currentPosition.toLong(), 1f)
        } else {
            builder.setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.currentPosition.toLong(), 1f)
        }
        mSession!!.setPlaybackState(builder.build())
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> start()
            else -> {
                var i = 0
                i++
            }
        }
    }
}