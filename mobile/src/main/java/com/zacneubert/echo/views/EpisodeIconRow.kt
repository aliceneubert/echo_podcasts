package com.zacneubert.echo.views

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.download.DownloadService
import com.zacneubert.echo.models.Episode

class EpisodeIconRow : RelativeLayout {
    constructor(context: Context, attributeSet: AttributeSet, defStyle: Int) : super(context, attributeSet, defStyle)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context) : super(context)

    private val grey = ContextCompat.getColor(context, R.color.textNeutral)
    private val blue = ContextCompat.getColor(context, R.color.colorPrimary)

    private var playedIcon : ImageButton
    private var downloadedIcon : ImageButton

    private var application: EchoApplication? = null
    private var episode: Episode? = null

    init {
        val rootView = View.inflate(context, R.layout.view_episode_icons, null)
        playedIcon = rootView.findViewById(R.id.episode_played_icon)
        downloadedIcon = rootView.findViewById(R.id.episode_download_icon)
        addView(rootView)
    }

    fun setEpisode(application: EchoApplication, episode: Episode) {
        this.episode = episode
        this.application = application

        playedIcon.setOnClickListener {
            episode.played = !episode.played
            application.episodeBox()!!.put(episode)
            refresh()
        }
        downloadedIcon.setOnClickListener {
            if(!episode.getFile(application).exists()) {
                episode.downloadFile(application)
            } else {
                episode.deleteFile(application)
            }
            refresh()
        }
        refresh()
    }

    private fun refresh() {
        if(episode != null) {
            playedIcon.setColorFilter(if(episode!!.played) grey else blue)
            downloadedIcon.setColorFilter(if(episode!!.getFile(context).exists()) blue else grey)
        }
    }
}
