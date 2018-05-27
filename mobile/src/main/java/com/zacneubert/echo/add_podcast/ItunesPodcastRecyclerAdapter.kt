package com.zacneubert.echo.add_podcast

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.zacneubert.echo.R
import com.zacneubert.echo.api.itunes.models.ItunesPodcast

class ItunesPodcastRecyclerAdapter(private val podcasts: Array<ItunesPodcast>) : RecyclerView.Adapter<ItunesPodcastRecyclerAdapter.ViewHolder>() {

    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    private fun asShortString(s: String) : String {
        return if (s.length < 50) s else s.substring(0, 50)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            val podcast = podcasts[position]

            val imageView = this.linearLayout.findViewById<TextView>(R.id.itunes_podcast_image) as ImageView
            Glide.with(imageView.context).load(podcast.artworkUrl600).into(imageView)

            val titleView = this.linearLayout.findViewById<TextView>(R.id.itunes_title) as TextView
            titleView.text = asShortString(podcast.collectionName)

            val artistView = this.linearLayout.findViewById<TextView>(R.id.itunes_artist) as TextView
            artistView.text = asShortString(podcast.artistName)

            val genreView = this.linearLayout.findViewById<TextView>(R.id.itunes_genre) as TextView
            genreView.text = asShortString(podcast.primaryGenreName)

            val podcast_item_add = this.linearLayout.findViewById<ImageButton>(R.id.itunes_podcast_item_add)
            podcast_item_add.setOnClickListener({
                Toast.makeText(podcast_item_add.context, "Bloopy-doopity-doop", Toast.LENGTH_SHORT).show();
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_itunes_podcast, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = podcasts.size
}
