package com.zacneubert.echo.episode_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast

class EpisodeListActivity : AppCompatActivity() {
    companion object {
        const val PODCAST_KEY = "PODCAST_KEY"

        fun ignitionIntent(context: Context, podcast: Podcast): Intent {
            val intent = Intent(context, EpisodeListActivity::class.java)
            intent.putExtra(PODCAST_KEY, podcast.id)
            return intent
        }
    }

    private lateinit var episodeRecycler: RecyclerView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var artView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var cardContainer: CardView
    private lateinit var refreshButton: ImageView
    private lateinit var deleteButton: ImageView

    private lateinit var podcast: Podcast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_list)

        episodeRecycler = findViewById(R.id.episode_recycler)
        titleView = findViewById(R.id.title)
        artistView = findViewById(R.id.description)
        artView = findViewById(R.id.podcast_art)
        progressBar = findViewById(R.id.loading)
        cardContainer = findViewById(R.id.podcast_card_container)

        refreshButton = findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener{
            refresh()
        }

        deleteButton = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener{
            (application as EchoApplication).podcastBox()!!.remove(podcast)
            EpisodeListActivity@this.finish()
        }

        val podcastId = intent.extras.get(PODCAST_KEY) as Long
        podcast = (application as EchoApplication).podcastBox()!!.get(podcastId)

        parseFeed()
    }

    private fun parseFeed() {
        titleView.text = podcast.title
        artistView.text = podcast.description

        if (podcast.artUri.isNotEmpty()) {
            Glide.with(artView.context).load(podcast.artUri).into(artView)
            artView.visibility = VISIBLE
        }

        setEpisodeList(podcast.episodes)
    }

    private fun setEpisodeList(episodes: List<Episode>) {
        val episodeArray = episodes.toTypedArray()
        episodeRecycler.adapter = EpisodeRecyclerAdapter(episodeArray)
        episodeRecycler.layoutManager = LinearLayoutManager(titleView.context)

        progressBar.visibility = GONE
        cardContainer.visibility = VISIBLE
        episodeRecycler.visibility = VISIBLE
    }

    private fun refresh() {
        cardContainer.visibility = GONE
        episodeRecycler.visibility = GONE
        progressBar.visibility = VISIBLE
        podcast.refreshEpisodeList(application = application as EchoApplication, onComplete = this::setEpisodeList)
    }
}