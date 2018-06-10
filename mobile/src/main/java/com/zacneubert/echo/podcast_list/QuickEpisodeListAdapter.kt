package com.zacneubert.echo.podcast_list

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.episode_list.EpisodeListActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast
import com.zacneubert.echo.player.MediaPlayerService


class QuickEpisodeListAdapter(private val innerContext: Context, private val episodes: Array<Episode>) : ArrayAdapter<Episode>(innerContext, R.layout.list_item_quick_episode, episodes) {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val episode = episodes[position]

        var rootView = convertView
        if(rootView == null) {
            rootView = LayoutInflater.from(innerContext).inflate(R.layout.list_item_quick_episode, parent, false)
        }

        val titleView = rootView!!.findViewById<TextView>(R.id.quick_episode_title) as TextView
        titleView.text = episode.title.substring(0, minOf(27, episode.title.length)) + "... " + episode.formattedDate()
        titleView.setOnClickListener {
            ContextCompat.startForegroundService(innerContext, MediaPlayerService.ignitionIntent(innerContext, episode))
        }

        return rootView
    }
}
