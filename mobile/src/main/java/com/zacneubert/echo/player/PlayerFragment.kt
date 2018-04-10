package com.zacneubert.echo.player

import android.app.Activity
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import java.util.*

class PlayerFragment : Fragment(), OnMediaStateChangeListener {
    private lateinit var backgroundLayout: RelativeLayout
    private lateinit var showTitle: TextView
    private lateinit var episodeTitle: TextView
    private lateinit var playButton: ImageButton
    private lateinit var skipBackButton: ImageButton
    private lateinit var skipForwardButton: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var seekProgress: TextView
    private lateinit var seekMaximum: TextView

    private var uiTimer: Timer = Timer()
    private var handler: Handler = Handler()

    companion object {
        fun newInstance(activity: Activity, episode: Episode, doStartService: Boolean): PlayerFragment {
            val playerFragment = PlayerFragment()
            if (doStartService) activity.startService(MediaPlayerService.ignitionIntent(activity, episode))
            return playerFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_player, container, false)

        backgroundLayout = rootView.findViewById(R.id.player_background)
        showTitle = rootView.findViewById(R.id.player_show_title)
        episodeTitle = rootView.findViewById(R.id.player_episode_title)
        playButton = rootView.findViewById(R.id.player_play)
        skipBackButton = rootView.findViewById(R.id.player_skip_back)
        skipForwardButton = rootView.findViewById(R.id.player_skip_forward)
        seekBar = rootView.findViewById(R.id.player_seek_bar)
        seekProgress = rootView.findViewById(R.id.player_progress_text)
        seekMaximum = rootView.findViewById(R.id.player_progress_max)

        playButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.togglePlaying()
        }

        skipBackButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.skipBackward()
        }

        skipForwardButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.skipForward()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MediaPlayerService.mediaPlayerService?.apply {
                        seekTo(progress)
                    }
                }
            }
        })

        uiTimer = Timer()
        uiTimer.schedule(object : TimerTask() {
            override fun run() {
                if (MediaPlayerService.mediaPlayerService != null) {
                    MediaPlayerService.mediaPlayerService?.addStateListener(this@PlayerFragment)
                    uiTimer.cancel()
                    uiTimer.purge()
                }
            }
        }, 10, 10)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        MediaPlayerService.mediaPlayerService?.addStateListener(this)
    }

    override fun onPause() {
        super.onPause()
        MediaPlayerService.mediaPlayerService?.removeStateListener(this)
    }

    fun longToTime(progress: Int): String {
        val totalSeconds = progress / 1000
        val seconds = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val minutes = totalMinutes % 60
        val hours = totalMinutes / 60

        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
    }

    override fun onMediaStateChange(mediaPlayer: MediaPlayer, episode: Episode) {
        handler.post({
            backgroundLayout.background = activity?.getDrawable(R.drawable.smash)

            if (mediaPlayer.isPlaying) {
                playButton.setImageDrawable(activity?.getDrawable(R.drawable.ic_pause_black_24dp))
            } else {
                playButton.setImageDrawable(activity?.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            }

            showTitle.text = episode.podcast.title
            episodeTitle.text = episode.title

            seekBar.max = mediaPlayer.duration
            seekBar.progress = mediaPlayer.currentPosition

            seekMaximum.text = longToTime(mediaPlayer.duration)
            seekProgress.text = longToTime(mediaPlayer.currentPosition)

        })
    }
}
