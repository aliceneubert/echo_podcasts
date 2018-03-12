package com.zacneubert.echo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.zacneubert.echo.models.Podcast

/**
 * Created by zac on 3/11/18.
 */
class PodcastListFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.podcast_list, container, false)
        val podcastRecycler = rootView.findViewById<RecyclerView>(R.id.podcast_recycler) as RecyclerView
        val podcasts = Array(100, {i -> Podcast(i)})
        podcastRecycler.adapter = PodcastRecyclerAdapter(podcasts)
        podcastRecycler.layoutManager = LinearLayoutManager(activity)
        return rootView
    }
}

class PodcastRecyclerAdapter(private val podcasts: Array<Podcast>) : RecyclerView.Adapter<PodcastRecyclerAdapter.ViewHolder>() {
    class ViewHolder(val linearLayout: LinearLayout) : RecyclerView.ViewHolder(linearLayout)

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        holder?.apply {
            val descriptionView = this.linearLayout.findViewById<TextView>(R.id.description) as TextView
            descriptionView.text = podcasts[position].description

            val titleView = this.linearLayout.findViewById<TextView>(R.id.title) as TextView
            titleView.text = podcasts[position].title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val rootLayout = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.podcast_detail, parent, false) as LinearLayout
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int = podcasts.size
}
