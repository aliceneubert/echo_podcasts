package com.zacneubert.echo.podcast_list

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.zacneubert.echo.episode_list.EpisodeListActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Podcast


class PodcastRecyclerAdapter(private val episodeSelectedListener: EpisodeSelectedListener, private val podcasts: Array<Podcast>) : RecyclerView.Adapter<PodcastRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val podcast = podcasts[position]

            val cardView = this.linearLayout.findViewById<TextView>(R.id.podcast_card_container) as CardView
            cardView.setOnClickListener {
                showEpisodes(cardView.context, podcast)
            }

            val artView = this.linearLayout.findViewById<TextView>(R.id.podcast_art) as ImageView
            Glide.with(artView.context).load(podcast.artUri).into(artView)

            val artistView = this.linearLayout.findViewById<TextView>(R.id.description) as TextView
            artistView.text = podcast.artist

            val titleView = this.linearLayout.findViewById<TextView>(R.id.title) as TextView
            titleView.text = podcast.title

            val podcast_item_play = this.linearLayout.findViewById<ImageButton>(R.id.podcast_item_play)
            podcast_item_play.setOnClickListener({
                if(podcast.episodes.isNotEmpty()) {
                    val newestEpisode = podcast.episodes.reversed()[0]
                    episodeSelectedListener.onEpisodeSelected(newestEpisode)
                }
            })

            val podcast_item_list_episodes = this.linearLayout.findViewById<ImageButton>(R.id.podcast_item_show_episodes)
            podcast_item_list_episodes.setOnClickListener({
                if(podcast.episodes.isNotEmpty()) {
                    AlertDialog.Builder(podcast_item_list_episodes.context)
                            .setTitle(podcast.title)
                            .setItems(podcast.episodes.map { e -> e.title }.toTypedArray(), object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface, which: Int) {
                                    episodeSelectedListener.onEpisodeSelected(podcast.episodes[which])
                                }
                            })
                            .show()
                }
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_podcast, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = podcasts.size

    fun showEpisodes(context : Context, podcast : Podcast) {
        context.startActivity(EpisodeListActivity.ignitionIntent(context, podcast))
    }
}
