package com.zacneubert.echo.playlist_list

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.R
import com.zacneubert.echo.episode_list.EpisodeRecyclerAdapter
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Playlist
import com.zacneubert.echo.player.MediaPlayerService
import kotlinx.android.synthetic.main.activity_playlist_detail.*

class PlaylistDetailActivity : AppCompatActivity() {
    companion object {
        const val PLAYLIST_KEY = "PLAYLIST_KEY"

        fun ignitionIntent(context: Context, playlist: Playlist): Intent {
            val intent = Intent(context, PlaylistDetailActivity::class.java)
            intent.putExtra(PLAYLIST_KEY, playlist.id)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    private lateinit var episodeRecycler: RecyclerView
    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var cardContainer: CardView
    private lateinit var reverseButton: ImageView
    private lateinit var deleteButton: ImageView
    private lateinit var shuffleButton: ImageView
    private lateinit var clearListenedButton: ImageView

    private lateinit var playlist: Playlist

    var reversed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_detail)

        episodeRecycler = findViewById(R.id.episode_recycler)
        titleView = findViewById(R.id.title)
        artistView = findViewById(R.id.description)
        cardContainer = findViewById(R.id.podcast_card_container)

        play_button.setOnClickListener {
            ContextCompat.startForegroundService(this, MediaPlayerService.ignitionIntent(this, playlist, playlist.nextUnplayed()!!))
        }

        shuffle_button.setOnClickListener {
            ContextCompat.startForegroundService(this, MediaPlayerService.ignitionIntent(this, playlist, playlist.episodeList().random(), true))
        }

        clear_listened_to.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("Clear listening data?")
                    .setMessage("This will clear all bookmarks and listening data, allowing you to listen to the playlist as though you had never started.")
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                        dialogInterface.cancel()
                    })
                    .setPositiveButton("Clear", DialogInterface.OnClickListener() { dialogInterface: DialogInterface, i: Int ->
                        val episodes = playlist.episodeList()
                        episodes.forEach {
                            it.played = false
                            it.lastStopTime = 0
                        }
                        (application as EchoApplication).episodeBox().put(episodes)
                        setContent()
                    })
                    .create()
                    .show()
        }

        reverseButton = findViewById(R.id.reverse_button)
        reverseButton.setOnClickListener {
            reversed = !reversed
        }

        deleteButton = findViewById(R.id.delete_button)
        deleteButton.setOnClickListener {
            (application as EchoApplication).playlistBox()!!.remove(playlist)
            EpisodeListActivity@ this.finish()
        }

        val playlistId = intent.extras?.get(PLAYLIST_KEY) as Long
        playlist = EchoApplication.instance(this).playlistBox().get(playlistId)

        setContent()
    }

    private fun setContent() {
        titleView.text = playlist.title
        artistView.text = playlist.description

        Glide.with(application).load(playlist.artUrl()).into(playlist_art)

        var episodes = playlist.episodeList()
        setEpisodeList(application as EchoApplication, playlist, episodes)
    }

    private fun setEpisodeList(application: EchoApplication, playlist: Playlist, episodes: List<Episode>) {
        val episodeArray = episodes.toTypedArray()
        episodeRecycler.adapter = EpisodeRecyclerAdapter(application as EchoApplication, episodeArray)
        episodeRecycler.layoutManager = LinearLayoutManager(titleView.context)

        cardContainer.visibility = VISIBLE
        episodeRecycler.visibility = VISIBLE
    }
}