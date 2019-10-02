package com.zacneubert.echo.player

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.player.MediaPlayerService.Companion.duration
import com.zacneubert.echo.player.MediaPlayerService.Companion.mediaControls
import com.zacneubert.echo.player.MediaPlayerService.Companion.metadata
import com.zacneubert.echo.player.MediaPlayerService.Companion.playbackState
import com.zacneubert.echo.player.MediaPlayerService.Companion.podcastTitle
import com.zacneubert.echo.settings.VolumeSetting
import java.util.*

class PlayerFragment : Fragment() {
    private lateinit var backgroundLayout: RelativeLayout
    private lateinit var showTitle: TextView
    private lateinit var episodeTitle: TextView
    private lateinit var playButton: ImageButton
    private lateinit var skipBackButton: ImageButton
    private lateinit var skipForwardButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var seekProgress: TextView
    private lateinit var seekMaximum: TextView
    private lateinit var podcastArt: ImageView
    private lateinit var episodeArt: ImageView
    private lateinit var volumeBar: SeekBar

    private var uiTimer: Timer = Timer()
    private var handler: Handler = Handler()

    companion object {
        fun newInstance(activity: Activity, episode: Episode, doStartService: Boolean): PlayerFragment {
            val playerFragment = PlayerFragment()
            if (doStartService) ContextCompat.startForegroundService(activity, MediaPlayerService.ignitionIntent(activity, episode))
            return playerFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_player, container, false)

        backgroundLayout = rootView.findViewById(R.id.player_background)
        showTitle = rootView.findViewById(R.id.player_show_title)
        episodeTitle = rootView.findViewById(R.id.player_episode_title)
        playButton = rootView.findViewById(R.id.player_play)
        skipBackButton = rootView.findViewById(R.id.player_skip_back)
        skipForwardButton = rootView.findViewById(R.id.player_skip_forward)
        seekBar = rootView.findViewById(R.id.player_seek_bar)
        seekProgress = rootView.findViewById(R.id.player_progress_text)
        seekMaximum = rootView.findViewById(R.id.player_progress_max)
        episodeArt = rootView.findViewById(R.id.episode_art)
        podcastArt = rootView.findViewById(R.id.podcast_art)
        volumeBar = rootView.findViewById(R.id.volume_bar)

        playButton.setOnClickListener {
            activity?.apply {
                playbackState(this)?.apply {
                    when (this.state) {
                        PlaybackStateCompat.STATE_PAUSED -> {
                            mediaControls(activity!!)?.play()
                        }
                        PlaybackStateCompat.STATE_PLAYING -> {
                            mediaControls(activity!!)?.pause()
                        }
                        else -> {
                            var i = 0
                            i++
                        }
                    }
                }
            }
        }

        skipBackButton.setOnClickListener {
            activity?.apply {
                mediaControls(this)?.skipToPrevious()
            }
        }

        skipForwardButton.setOnClickListener {
            activity?.apply {
                mediaControls(this)?.skipToNext()
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    activity?.apply {
                        mediaControls(this)?.seekTo(progress.toLong())
                    }
                }
            }
        })

        volumeBar.max = MediaPlayerService.MAX_VOLUME
        volumeBar.progress = VolumeSetting.get(rootView.context).toInt()
        volumeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    activity?.apply {
                        MediaPlayerService.mediaPlayerService.apply {
                            this!!.setVolume(progress)
                        }
                    }
                }
            }
        })

        return rootView
    }

    override fun onResume() {
        super.onResume()
        uiTimer = Timer()
        uiTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                onMediaStateChange()
            }
        }, 0, 10)
    }

    override fun onPause() {
        super.onPause()
        uiTimer.cancel()
        uiTimer.purge()
    }

    private fun longToTime(progress: Long): String {
        val totalSeconds = progress / 1000
        val seconds = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val minutes = totalMinutes % 60
        val hours = totalMinutes / 60

        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
    }

    fun onMediaStateChange() {
        handler.post({
            if (activity == null) return@post
            val context: Context = activity as FragmentActivity

            metadata(context)?.apply {
                episodeTitle.text = this.description.title
                showTitle.text = podcastTitle(context)
                duration(context)?.apply {
                    seekBar.max = this.toInt()
                    seekMaximum.text = longToTime(this)
                }

                val albumArtUriString = this.getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI)
                if(albumArtUriString != null) {
                    Glide.with(context).load(Uri.parse(albumArtUriString)).into(podcastArt)
                }

                val episodeArtUriString = this.getString(android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI)
                if(episodeArtUriString != null) {
                    Glide.with(context).load(Uri.parse(episodeArtUriString)).into(episodeArt)
                }
            }

            playbackState(context)?.apply {
                if (this.state == PlaybackStateCompat.STATE_PLAYING) {
                    playButton.setImageDrawable(activity?.getDrawable(R.drawable.ic_pause_black_24dp))
                } else {
                    playButton.setImageDrawable(activity?.getDrawable(R.drawable.ic_play_arrow_black_24dp))
                }

                seekBar.progress = this.position.toInt()
                seekProgress.text = longToTime(this.position)
            }
        })
    }
}
