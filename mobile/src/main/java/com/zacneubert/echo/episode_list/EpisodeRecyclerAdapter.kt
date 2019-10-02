package com.zacneubert.echo.episode_list

import android.content.Context
import android.provider.MediaStore
import android.service.voice.VoiceInteractionService
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.player.MediaPlayerService
import com.zacneubert.echo.views.EpisodeIconRow


class EpisodeRecyclerAdapter(private val application: EchoApplication, private val episodes: Array<Episode>) : RecyclerView.Adapter<EpisodeRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val episode = episodes[position]

            val cardView = this.linearLayout.findViewById<TextView>(R.id.episode_card_container) as CardView
            cardView.setOnClickListener {
                playEpisode(cardView.context, episode)
            }

            val artView = this.linearLayout.findViewById<TextView>(R.id.episode_art) as ImageView
            if(episode.artUrl.isNotEmpty()) {
                Glide.with(artView.context).load(episode.artUrl).into(artView)
                artView.visibility = VISIBLE
            } else {
                artView.visibility = GONE
            }

            val titleView = this.linearLayout.findViewById<TextView>(R.id.title) as TextView
            titleView.text = episode.title

            val dateView = this.linearLayout.findViewById<TextView>(R.id.publishDate) as TextView
            dateView.text = episode.formattedDate()

            val descriptionView = this.linearLayout.findViewById<TextView>(R.id.description) as TextView
            descriptionView.text = Html.fromHtml(episode.description)

            val episodeIconRow = this.linearLayout.findViewById<EpisodeIconRow>(R.id.episode_icon_row) as EpisodeIconRow
            episodeIconRow.setEpisode(application, episode)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context).inflate(R.layout.list_item_episode, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = episodes.size

    private fun playEpisode(context: Context, episode: Episode) {
        ContextCompat.startForegroundService(context, MediaPlayerService.ignitionIntent(context, episode))
    }
}
