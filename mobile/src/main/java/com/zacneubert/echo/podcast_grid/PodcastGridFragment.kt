package com.zacneubert.echo.podcast_grid

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.add_podcast.AddPodcastActivity
import com.zacneubert.echo.add_podcast.AddPodcastFromLinkActivity
import com.zacneubert.echo.download.DownloadProgressReceiver
import com.zacneubert.echo.podcast_list.EpisodeSelectedListener

class PodcastGridFragment : Fragment() {
    private lateinit var episodeSelectedListener: EpisodeSelectedListener
    private lateinit var podcastRecycler: RecyclerView
    private lateinit var rootView: View

    companion object {
        fun newInstance(episodeSelectedListener: EpisodeSelectedListener): PodcastGridFragment {
            val podcastListFragment = PodcastGridFragment()
            podcastListFragment.episodeSelectedListener = episodeSelectedListener
            return podcastListFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_podcast_list, container, false)

        val searchPodcastFab = rootView.findViewById<com.github.clans.fab.FloatingActionButton>(R.id.search_podcast_fab)
        searchPodcastFab.setOnClickListener {
            ContextCompat.startActivity(it.context, Intent(it.context, AddPodcastActivity::class.java), null)
        }

        val addByLinkFab = rootView.findViewById<com.github.clans.fab.FloatingActionButton>(R.id.add_by_link_fab)
        addByLinkFab.setOnClickListener {
            ContextCompat.startActivity(it.context, Intent(it.context, AddPodcastFromLinkActivity::class.java), null)
        }

        podcastRecycler = rootView.findViewById<RecyclerView>(R.id.podcast_recycler) as RecyclerView
        initPodcastGrid()

        return rootView
    }

    var downloadReceiver: DownloadProgressReceiver? = null
    override fun onResume() {
        super.onResume()
        initPodcastGrid()

        downloadReceiver = DownloadProgressReceiver { episodeId: Long, percent: Double ->
            if (percent > .98) {
                rootView.invalidate()
            }
        }

        activity!!.registerReceiver(downloadReceiver, DownloadProgressReceiver.getIntentFilter())
    }

    override fun onPause() {
        activity!!.unregisterReceiver(downloadReceiver)
        super.onPause()
    }

    private fun initPodcastGrid() {
        val application = activity!!.application as EchoApplication
        val podcasts = application.chronologicalPodcasts()
        podcastRecycler.adapter = PodcastGridRecyclerAdapter(application, podcasts.toTypedArray())
        podcastRecycler.layoutManager = GridLayoutManager(activity, 3)
    }
}
