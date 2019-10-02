package com.zacneubert.echo.add_podcast

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import com.github.clans.fab.FloatingActionButton
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.models.Podcast
import kotlinx.android.synthetic.main.activity_add_podcast_from_link.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread

class AddPodcastFromLinkActivity : Activity() {
    companion object {
        const val LINK_EXTRA = "LINK_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_podcast_from_link)

        if(intent.hasExtra(Intent.EXTRA_TEXT)) {
            link_input.setText(intent.getStringExtra(Intent.EXTRA_TEXT))
        }

        if(intent.hasExtra(LINK_EXTRA)) {
            link_input.setText(intent.getStringExtra(LINK_EXTRA))
        }


        val submitButton = findViewById<FloatingActionButton>(R.id.link_submit)
        submitButton.setOnClickListener {
            link_card.visibility = GONE
            link_loading.visibility = VISIBLE
            doAsync {
                val podcast = Podcast(link_input.text.toString())
                if (podcast.checkFeedUrl()) {
                    podcast.getFeedAndUpdateAttrs(application as EchoApplication)
                    uiThread {
                        podcast.refreshEpisodeList(application = application as EchoApplication, onComplete = null)
                        Toast.makeText(this@AddPodcastFromLinkActivity, "Podcast Added!", Toast.LENGTH_SHORT).show()
                        startActivity(MainActivity.ignitionIntent(this@AddPodcastFromLinkActivity, MainActivity.MainFragmentChoice.PODCAST))
                        this@AddPodcastFromLinkActivity.finish()
                    }
                } else {
                    uiThread {
                        link_card.visibility = VISIBLE
                        link_loading.visibility = GONE
                        Toast.makeText(this@AddPodcastFromLinkActivity, "Link does not appear to be a valid feed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}