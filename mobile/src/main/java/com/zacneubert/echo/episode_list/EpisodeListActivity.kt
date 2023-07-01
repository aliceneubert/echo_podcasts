package com.zacneubert.echo.episode_list

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Playlist
import com.zacneubert.echo.models.PlaylistEpisode
import com.zacneubert.echo.models.Podcast
import kotlinx.android.synthetic.main.activity_episode_list.*

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
    private lateinit var reverseButton: ImageView
    private lateinit var deleteButton: ImageView

    private lateinit var filterView: EditText

    private lateinit var podcast: Podcast

    var reversed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_list)

        episodeRecycler = findViewById(R.id.episode_recycler)
        titleView = findViewById(R.id.title)
        artistView = findViewById(R.id.description)
        artView = findViewById(R.id.podcast_art)
        progressBar = findViewById(R.id.loading)
        cardContainer = findViewById(R.id.podcast_card_container)

        filterView = findViewById(R.id.filter_field)
        filterView.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filterFeed()
            }
        })

        save_as_playlist_button.setOnClickListener {
            var playlistNameEditText = EditText(this)
            playlistNameEditText.text = filterView.text
            AlertDialog.Builder(this)
                    .setTitle("Set Playlist Title:")
                    .setView(playlistNameEditText)
                    .setPositiveButton("Save"
                    ) { dialog, which ->
                        var echoApplication: EchoApplication = EchoApplication.instance(this)
                        var playlist = Playlist()
                        playlist.title = playlistNameEditText.text.toString()
                        playlist.description = podcast.title + " filtered by " + filterView.text
                        echoApplication.playlistBox().put(playlist)

                        var position = 0L
                        var playlistEpisodes = filteredEpisodes().map {
                            var playlistEpisode = PlaylistEpisode()
                            playlistEpisode.episode.target = it
                            position += 1
                            playlistEpisode.position = position
                            playlistEpisode.playlist.target = playlist

                            echoApplication.playlistEpisodeBox().put(playlistEpisode)
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, which ->

                    }
                    .show()
        }

        reverseButton = findViewById(R.id.reverse_button)
        reverseButton.setOnClickListener {
            reversed = !reversed
            filterFeed()
        }

        refreshButton = findViewById(R.id.refresh_button)
        refreshButton.setOnClickListener {
            refresh()
        }

        deleteButton = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            (application as EchoApplication).podcastBox()!!.remove(podcast)
            EpisodeListActivity@ this.finish()
        }

        val podcastId = intent.extras?.get(PODCAST_KEY) as Long
        podcast = (application as EchoApplication).podcastBox()!!.get(podcastId)

        filterFeed()
    }

    private fun filteredEpisodes(): List<Episode> {
        var episodes = podcast.chronologicalEpisodes()

        val filterTerm = filterView.text.trim().toString().toLowerCase()
        if (filterTerm != "") {
            episodes = episodes.filter { e ->
                e.title.toLowerCase().contains(filterTerm) ||
                        e.description.toLowerCase().contains(filterTerm)
            }
        }

        if (reversed) {
            episodes = episodes.reversed()
        }

        return episodes
    }

    private fun filterFeed() {
        titleView.text = podcast.title
        artistView.text = podcast.description

        if (podcast.artUri.isNotEmpty()) {
            Glide.with(artView.context).load(podcast.artUri).into(artView)
            artView.visibility = VISIBLE
        }

        if (reversed) {
            reverseButton.setImageResource(R.drawable.ic_arrow_upward_black_24dp)
        } else {
            reverseButton.setImageResource(R.drawable.ic_arrow_downward_black_24dp)
        }

        var episodes = filteredEpisodes()
        setEpisodeList(application as EchoApplication, podcast, episodes)
    }

    private fun setEpisodeList(application: EchoApplication, podcast: Podcast, episodes: List<Episode>) {
        val episodeArray = episodes.toTypedArray()
        episodeRecycler.adapter = EpisodeRecyclerAdapter(application as EchoApplication, episodeArray)
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