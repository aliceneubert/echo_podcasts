package com.zacneubert.echo.podcast_list

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.player.MediaPlayerService
import com.zacneubert.echo.views.EpisodeIconRow


class QuickEpisodeListAdapter(private val application: EchoApplication, private val episodes: Array<Episode>) : ArrayAdapter<Episode>(application, R.layout.list_item_quick_episode, episodes) {
    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val episode = episodes[position]

        var rootView = convertView
        if(rootView == null) {
            rootView = LayoutInflater.from(application).inflate(R.layout.list_item_quick_episode, parent, false)
        }

        val titleContainer = rootView!!.findViewById<TextView>(R.id.quick_episode_title_container) as LinearLayout
        titleContainer.setOnClickListener {
            ContextCompat.startForegroundService(application, MediaPlayerService.ignitionIntent(application, episode))
        }

        val titleView = rootView!!.findViewById<TextView>(R.id.quick_episode_title) as TextView
        titleView.text = episode.title

        val dateView = rootView!!.findViewById<TextView>(R.id.quick_episode_date) as TextView
        dateView.text = episode.formattedDate()

        val iconRow = rootView!!.findViewById<TextView>(R.id.quick_icon_row) as EpisodeIconRow
        iconRow.setEpisode(application, episode)

        return rootView
    }
}
