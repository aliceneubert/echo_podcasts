package com.zacneubert.echo.playlist_list

import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Playlist
import com.zacneubert.echo.player.MediaPlayerService


class PlaylistRecyclerAdapter(private val application: EchoApplication, private val playlists: Array<Playlist>) : RecyclerView.Adapter<PlaylistRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val playlist = playlists[position]

            val cardView = this.linearLayout.findViewById<TextView>(R.id.podcast_card_container) as CardView
            cardView.setOnClickListener {
                ContextCompat.startActivity(application, PlaylistDetailActivity.ignitionIntent(application, playlist), null)
            }

            cardView.setOnLongClickListener {
                if (playlist.nextUnplayed() != null) {
                    ContextCompat.startForegroundService(application, MediaPlayerService.ignitionIntent(application, playlist, playlist.nextUnplayed()!!))
                } else if (!playlist.episodeList().isEmpty()) {
                    ContextCompat.startForegroundService(application, MediaPlayerService.ignitionIntent(application, playlist, playlist.episodeList().first()))
                } else {
                    Toast.makeText(application, "Playlist has no episodes!", Toast.LENGTH_SHORT).show()
                }
                true
            }

            val titleView = this.linearLayout.findViewById<TextView>(R.id.title) as TextView
            titleView.text = playlist.title

            val descriptionView = this.linearLayout.findViewById<TextView>(R.id.description) as TextView
            descriptionView.text = playlist.description

            val nextUpView = this.linearLayout.findViewById<TextView>(R.id.next_episode_title) as TextView
            val nextUpArt = this.linearLayout.findViewById<TextView>(R.id.next_episode_art) as ImageView
            nextUpView.text = "Next Up: " + playlist.nextUnplayed()?.title
            Glide.with(application).load(playlist.artUrl()).into(nextUpArt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context).inflate(R.layout.list_item_playlist, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = playlists.size
}
