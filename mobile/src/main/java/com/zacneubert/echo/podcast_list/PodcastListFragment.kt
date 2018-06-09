package com.zacneubert.echo.podcast_list

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.add_podcast.AddPodcastActivity
import com.zacneubert.echo.helpers.LegacyPodcastProvider
import com.zacneubert.echo.models.Podcast
import java.util.Collections.max

class PodcastListFragment : Fragment() {
    private lateinit var episodeSelectedListener: EpisodeSelectedListener
    private lateinit var podcastRecycler : RecyclerView

    companion object {
        fun newInstance(mainActivity: MainActivity): PodcastListFragment {
            val podcastListFragment = PodcastListFragment()
            podcastListFragment.episodeSelectedListener = mainActivity
            return podcastListFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_podcast_list, container, false)

        val addPodcastButton = rootView.findViewById<FloatingActionButton>(R.id.add_podcast_fab) as FloatingActionButton
        addPodcastButton.setOnClickListener({
            ContextCompat.startActivity(it.context, Intent(it.context, AddPodcastActivity::class.java), null)
        })

        podcastRecycler = rootView.findViewById<RecyclerView>(R.id.podcast_recycler) as RecyclerView
        setPodcastList()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        setPodcastList()
    }

    private fun setPodcastList() {
        val podcasts = (activity!!.application as EchoApplication).boxStore.boxFor(Podcast::class.java).all
        podcastRecycler.adapter = PodcastRecyclerAdapter(episodeSelectedListener, podcasts.toTypedArray())
        podcastRecycler.layoutManager = LinearLayoutManager(activity)
    }
}
