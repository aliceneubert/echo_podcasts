package com.zacneubert.echo.podcast_list

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.episode_list.EpisodeListActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Podcast


class PodcastRecyclerAdapter(private val application: EchoApplication, private val podcasts: Array<Podcast>) : RecyclerView.Adapter<PodcastRecyclerAdapter.ViewHolder>() {

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

            val quickEpisodeListView = this.linearLayout.findViewById<TextView>(R.id.quick_episode_list) as ListView

            val total_episodes = podcast.chronologicalEpisodes().size
            val quick_episodes = podcast.chronologicalEpisodes().subList(0, minOf(3, total_episodes)).toTypedArray()
            quickEpisodeListView.adapter = QuickEpisodeListAdapter(application, quick_episodes)
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
