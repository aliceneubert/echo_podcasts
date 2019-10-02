package com.zacneubert.echo.playlist_list

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R

class PlaylistListFragment : Fragment() {
    private lateinit var podcastRecycler: RecyclerView
    private lateinit var rootView: View

    companion object {
        fun newInstance(mainActivity: MainActivity): PlaylistListFragment {
            val podcastListFragment = PlaylistListFragment()
            return podcastListFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_playlist_list, container, false)

        /*val searchPodcastFab = rootView.findViewById<com.github.clans.fab.FloatingActionButton>(R.id.search_podcast_fab)
        searchPodcastFab.setOnClickListener {
            ContextCompat.startActivity(it.context, Intent(it.context, AddPodcastActivity::class.java), null)
        }

        val addByLinkFab = rootView.findViewById<com.github.clans.fab.FloatingActionButton>(R.id.add_by_link_fab)
        addByLinkFab.setOnClickListener {
            ContextCompat.startActivity(it.context, Intent(it.context, AddPodcastFromLinkActivity::class.java), null)
        }*/

        podcastRecycler = rootView.findViewById<RecyclerView>(R.id.podcast_recycler) as RecyclerView
        setPlaylistList()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        setPlaylistList()
    }

    override fun onPause() {
        super.onPause()
    }

    private fun setPlaylistList() {
        val application = EchoApplication.instance(this.activity!!)
        val podcasts = application.playlistBox().all
        podcastRecycler.adapter = PlaylistRecyclerAdapter(application, podcasts.toTypedArray())
        podcastRecycler.layoutManager = LinearLayoutManager(activity)
    }
}
