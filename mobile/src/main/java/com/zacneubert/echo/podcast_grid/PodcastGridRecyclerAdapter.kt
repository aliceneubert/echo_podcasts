package com.zacneubert.echo.podcast_grid

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.episode_list.EpisodeListActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast
import com.zacneubert.echo.player.MediaPlayerService


class PodcastGridRecyclerAdapter(private val application: EchoApplication, private val podcasts: Array<Podcast>) : RecyclerView.Adapter<PodcastGridRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val podcast = podcasts[position]

            val cardView = this.linearLayout.findViewById<TextView>(R.id.podcast_card_container) as LinearLayout
            cardView.setOnClickListener {
                showEpisodes(cardView.context, podcast)
            }

            cardView.setOnLongClickListener {
                val episode: Episode? = podcast.episodes.filter { e -> !e.played }.firstOrNull()
                episode.apply {
                    ContextCompat.startForegroundService(application, MediaPlayerService.ignitionIntent(application, episode!!))
                }
                true
            }

            val artView = this.linearLayout.findViewById<TextView>(R.id.podcast_art) as ImageView
            Glide.with(artView.context).load(podcast.artUri).into(artView)

            val badgeView = this.linearLayout.findViewById<View>(R.id.unlistened_badge)
            val badgeTextView = this.linearLayout.findViewById<TextView>(R.id.unlistened_badge_text)

            when(val recentUnplayedCount = podcast.topNChronological(5).filter { e -> !e.played }.size) {
                0 -> badgeView.visibility = View.GONE
                1, 2, 3 -> {
                    badgeView.visibility = View.VISIBLE
                    badgeTextView.text = recentUnplayedCount.toString()
                }
                else -> {
                    badgeView.visibility = View.VISIBLE
                    badgeTextView.text = "3+"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.grid_item_podcast, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = podcasts.size

    private fun showEpisodes(context : Context, podcast : Podcast) {
        context.startActivity(EpisodeListActivity.ignitionIntent(context, podcast))
    }
}
