package com.zacneubert.echo.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationCompat.PRIORITY_MAX
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.app.NotificationCompat.MediaStyle
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.helpers.LegacyPodcastProvider
import com.zacneubert.echo.helpers.TimeMillis
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast
import java.io.File


class MediaPlayerService : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {
    companion object {
        const val EPISODE_KEY: String = "EPISODE_KEY"
        const val CHANNEL_ID: String = "Media Player Notification Channel"
        const val NOTIFICATION_ID: Int = 6
        const val MEDIA_SESSION_TOKEN: String = "MediaPlayerService"

        const val MEDIA_ROOT_ID: String = "ECHO_MEDIA_ROOT"

        var mediaPlayerService: MediaPlayerService? = null
            private set

        fun ignitionIntent(context: Context, episode: Episode): Intent {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.putExtra(EPISODE_KEY, episode)
            return intent
        }

        fun mediaController(context: Context): MediaControllerCompat? {
            MediaPlayerService.mediaPlayerService?.token?.apply {
                return MediaControllerCompat(context, this)
            }
            return null
        }

        fun mediaControls(context: Context): MediaControllerCompat.TransportControls? {
            return mediaController(context)?.transportControls
        }

        fun playbackState(context: Context): PlaybackStateCompat? {
            return mediaController(context)?.playbackState
        }

        fun metadata(context: Context): MediaMetadataCompat? {
            return mediaController(context)?.metadata
        }

        fun duration(context: Context): Long? {
            return metadata(context)?.bundle?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        }

        fun podcastTitle(context: Context): String? {
            return metadata(context)?.bundle?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        }
    }

    lateinit var episode: Episode
    var mediaPlayer: MediaPlayer = MediaPlayer()
        private set

    private var playbackState: PlaybackStateCompat.Builder = PlaybackStateCompat.Builder()

    private var mSession: MediaSessionCompat? = null
    val token: MediaSessionCompat.Token?
        get() {
            return mSession?.sessionToken
        }

    private var mAudioManager: AudioManager? = null
    private var mMediaCallback: MediaSessionCallback = MediaSessionCallback()

    private var playerPrepared: Boolean = false

    override fun onCreate() {
        super.onCreate()

        startForeground(NOTIFICATION_ID, buildSimplifiedNotification())

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

        startForeground(NOTIFICATION_ID, buildNotification(true))

        initializeMediaPlayer()

        mediaPlayer.setOnErrorListener { mp, what, extra ->
            initializeMediaPlayer()
            true
        }

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
            mMediaCallback.onPlay()
            setMetadata()
            mMediaCallback.onSeekTo(getStopTime().toLong())
        }
        mediaPlayer.prepareAsync()
    }


    private fun setMetadata() {
        mSession!!.setMetadata(
                MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, episode.podcast.target.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, episode.podcast.target.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, episode.title)
                        .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, episode.title)
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.duration.toLong())
                        .build()
        )
    }

    private fun buildNotificationAction(keyCode: Int): PendingIntent {
        val mediaIntent = Intent(this, MediaIntentReceiver::class.java)
        mediaIntent.putExtra(MediaIntentReceiver.MEDIA_INTENT_KEY_CODE, keyCode)
        return PendingIntent.getBroadcast(this, keyCode, mediaIntent, 0)
    }

    private fun buildNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)
            notificationChannel.lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            notificationChannel.enableVibration(false)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildSimplifiedNotification(): Notification {
        buildNotificationChannel()

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentTitle("Setting up player...")
                .setPriority(PRIORITY_MAX)
                .build()
    }

    private fun buildNotification(isPlaying: Boolean = mediaPlayer.isPlaying): Notification {
        buildNotificationChannel()

        val openAppIntent = MainActivity.ignitionIntent(this, MainActivity.MainFragmentChoice.NOW_PLAYING)
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, 0)

        val skipBackAction = buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD)
        val pausePlayAction = buildNotificationAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        val skipForwardAction = buildNotificationAction(KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD)

        val style: MediaStyle = MediaStyle().setMediaSession(mSession!!.sessionToken).setShowActionsInCompactView(0, 1, 2)

        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.echo_logo_placeholder)
                .setContentText(episode.podcast.target.title)
                .setContentTitle(episode.title)
                .setPriority(PRIORITY_MAX)
                .addAction(R.drawable.ic_skip_previous_black_24dp, "Previous", skipBackAction)
                .addAction(if (isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp, "Pause", pausePlayAction)
                .addAction(R.drawable.ic_skip_next_black_24dp, "Next", skipForwardAction)
                .setStyle(style)
                .setContentIntent(openAppPendingIntent)
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

    private fun audioFocusRequest(): AudioFocusRequest {
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

    private fun audioFocusReleaseRequest() : AudioFocusRequest {
        val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        return AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_LOSS)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener(this)
                .build()
    }

    fun saveStopTime() {
        val stopTime = mediaPlayer.currentPosition
        if (stopTime > 10 * TimeMillis.SECOND) { // Never save less than 10 seconds in, to avoid overwriting better save times
            episode.lastStopTime = stopTime.toLong()
        }
    }

    private fun getStopTime(): Int {
        return episode.lastStopTime.toInt()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {
        val mediaItems = mutableListOf<MediaItem>()
        if (parentId == MEDIA_ROOT_ID) {
            val podcasts = listOf<Podcast>()
            podcasts.forEach {
                mediaItems.add(MediaItem(MediaDescriptionCompat.Builder()
                        .setTitle(it.title)
                        .setDescription(it.description)
                        .setMediaId(it.id.toString())
                        .build(), MediaItem.FLAG_BROWSABLE))
            }
        } else {
            val podcast = Podcast()
            listOf<Episode>()
                    .forEach {
                        mediaItems.add(MediaItem(MediaDescriptionCompat.Builder()
                                .setTitle(it.title)
                                .setMediaUri(it.getUri())
                                .setMediaId(it.id.toString())
                                .build(), MediaItem.FLAG_PLAYABLE))
                    }
        }
        result.sendResult(mediaItems)
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            mAudioManager!!.requestAudioFocus(audioFocusRequest())
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
            repostNotification()
            broadcastPlaybackState()
        }

        override fun onSkipToQueueItem(queueId: Long) {}

        override fun onSeekTo(position: Long) {
            mediaPlayer.seekTo(position.toInt())
            broadcastPlaybackState()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            //TODO make this work w/ objectbox
            //val episodeFile = File(mediaId)
            //val podcast = Podcast(episodeFile.parentFile)
            //val episode = Episode(podcast, episodeFile)
            //ContextCompat.startForegroundService(applicationContext, MediaPlayerService.ignitionIntent(applicationContext, episode))
        }

        override fun onPause() {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                saveStopTime()
            }
            repostNotification()
            broadcastPlaybackState()
        }

        override fun onStop() {
            mediaPlayer.stop()
            broadcastPlaybackState()
        }

        override fun onSkipToNext() {
            val seconds = 30
            val skipTo = Math.min(mediaPlayer.currentPosition + seconds * 1000, mediaPlayer.duration)
            mediaPlayer.seekTo(skipTo)
            broadcastPlaybackState()
        }

        override fun onSkipToPrevious() {
            val seconds = 30
            val skipTo = Math.max(mediaPlayer.currentPosition - seconds * 1000, 0)
            mediaPlayer.seekTo(skipTo)
            broadcastPlaybackState()
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            var i = 0
            i++
        }

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            var i = 0
            i++
        }
    }


    fun broadcastPlaybackState() {
        var builder = playbackState
        builder = if (mediaPlayer.isPlaying) {
            builder.setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.currentPosition.toLong(), 1f)
            builder.setActions(PlaybackStateCompat.ACTION_PAUSE or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        } else {
            builder.setState(PlaybackStateCompat.STATE_PAUSED, mediaPlayer.currentPosition.toLong(), 1f)
            builder.setActions(PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
        }
        mSession!!.setPlaybackState(builder.build())
    }

    var lastDucked: Long = 0
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if(System.currentTimeMillis() - lastDucked < TimeMillis.SECOND * 10) {
                    mMediaCallback.onPlay()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK,
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                try {
                    if (playerPrepared and mediaPlayer.isPlaying) {
                        mMediaCallback.onPause()
                        lastDucked = System.currentTimeMillis()
                    }
                } catch (e : IllegalStateException) {
                    // Don't die here plz
                }
            }
            else -> {
                var i = 0
                i++
            }
        }
    }
}