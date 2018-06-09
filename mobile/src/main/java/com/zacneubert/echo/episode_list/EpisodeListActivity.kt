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
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Podcast
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_list)

        episodeRecycler = findViewById(R.id.episode_recycler)
        titleView = findViewById(R.id.title)
        artistView = findViewById(R.id.description)
        artView = findViewById(R.id.podcast_art)
        progressBar = findViewById(R.id.loading)
        cardContainer = findViewById(R.id.podcast_card_container)

        val podcastId = intent.extras.get(PODCAST_KEY) as Long
        val podcast = (application as EchoApplication).podcastBox()!!.get(podcastId)

        parseFeed(podcast = podcast)
    }

    private fun parseFeed(podcast: Podcast) {
        doAsync {
            val feedUrl = URL(podcast.feedUrl)

            val syndFeedInput = SyndFeedInput()
            val feed = syndFeedInput.build(XmlReader(feedUrl))

            uiThread {
                titleView.text = feed.title.toString()
                artistView.text = feed.description.toString()

                feed.image?.apply {
                    Glide.with(artView.context).load(this.url).into(artView)
                    artView.visibility = VISIBLE
                }

                progressBar.visibility = GONE
                cardContainer.visibility = VISIBLE

                val episodes = feed.entries.map { e -> Episode(podcast, e) }.toTypedArray()
                episodeRecycler.adapter = EpisodeRecyclerAdapter(episodes)
                episodeRecycler.layoutManager = LinearLayoutManager(titleView.context)
            }
        }
    }
}