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

        MediaPlayerService.mediaPlayerService?.apply {
            onMediaStateChange(this)
        }

        playButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.apply {
                this.togglePlaying()
            }
        }

        skipBackButton = rootView.findViewById(R.id.player_skip_back)
        skipBackButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.apply {
                this.skipBackward()
            }
        }

        skipForwardButton = rootView.findViewById(R.id.player_skip_forward)
        skipForwardButton.setOnClickListener {
            MediaPlayerService.mediaPlayerService?.apply {
                this.skipForward()
            }
        }

        uiTimer = Timer()
        uiTimer.schedule(object : TimerTask() {
            override fun run() {
                if (MediaPlayerService.mediaPlayerService != null) {
                    onMediaStateChange(MediaPlayerService.mediaPlayerService!!)
                    uiTimer.cancel()
                    uiTimer.purge()
                }
            }
        }, 10, 10)

        return rootView
    }

    override fun onResume() {
        super.onResume()
        MediaPlayerService.mediaPlayerService?.apply {
            mediaStateChangeListeners.add(this@PlayerFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        MediaPlayerService.mediaPlayerService?.apply {
            if (mediaStateChangeListeners.contains(this@PlayerFragment)) {
                mediaStateChangeListeners.remove(this@PlayerFragment)
            }
        }
    }

    fun onMediaStateChange(mediaPlayerService: MediaPlayerService) {
        onMediaStateChange(mediaPlayerService.mediaPlayer, mediaPlayerService.episode)
    }

    override fun onMediaStateChange(mediaPlayer: MediaPlayer, episode: Episode) {
        handler.post({
            backgroundLayout.background = activity.getDrawable(R.drawable.smash)

            if (mediaPlayer.isPlaying) {
                playButton.setImageDrawable(activity.getDrawable(R.drawable.ic_pause_black_24dp))
            } else {
                playButton.setImageDrawable(activity.getDrawable(R.drawable.ic_play_arrow_black_24dp))
            }

            showTitle.text = episode.podcast.title
            episodeTitle.text = episode.title
        })
    }
}
