package com.zacneubert.echo.settings

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.MainActivity
import com.zacneubert.echo.R
import com.zacneubert.echo.download.MassDownloadSetupService
import com.zacneubert.echo.download.scheduleEveryHour
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.Episode_
import com.zacneubert.echo.models.Podcast

class SettingsFragment : Fragment() {
    companion object {
        fun newInstance(): SettingsFragment {
            val podcastListFragment = SettingsFragment()
            return podcastListFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)

        val context = rootView.context
        val refreshPodcastsButton = rootView.findViewById<Button>(R.id.refresh_button) as Button
        refreshPodcastsButton.setOnClickListener { _ ->
            val application = activity!!.application as EchoApplication
            application.podcastBox().all.forEach {
                it.refreshEpisodeList(application, this::onRefreshComplete)
            }

            application.episodeBox().all.forEach {
                if (it.podcast.target == null) {
                    application.episodeBox().remove(it)
                }
            }
        }

        val triggerSchedulingButton = rootView.findViewById<Button>(R.id.trigger_scheduling_button) as Button
        triggerSchedulingButton.setOnClickListener {
            scheduleEveryHour(context)
        }

        val massDownloadButton = rootView.findViewById<Button>(R.id.mass_download_button) as Button
        massDownloadButton.setOnClickListener {
            ContextCompat.startForegroundService(
                    context,
                    MassDownloadSetupService.ignitionIntent(context))
        }

        (rootView.findViewById<Button>(R.id.delete_all_episodes) as Button). setOnClickListener {
            val box = (context.applicationContext as EchoApplication).episodeBox()
            var filesDeleted = 0
            var episodesDeleted = 0
            box.all.forEach {
                if (it.fileExists(context)) {
                    it.deleteFile(context)
                    filesDeleted += 1
                }
                box.remove(it)
                episodesDeleted += 1
            }
            Toast.makeText(context, "Deleted %d episodes and %d files".format(episodesDeleted, filesDeleted), Toast.LENGTH_LONG).show()
        }

        (rootView.findViewById<Button>(R.id.clean_db_button) as Button). setOnClickListener {
            val box = (context.applicationContext as EchoApplication).episodeBox()
            var filesDeleted = 0
            var episodesDeleted = 0
            box.all.forEach {
                if (it.podcast.target == null) {
                    if (it.fileExists(context)) {
                        it.deleteFile(context)
                        filesDeleted += 1
                    }
                    box.remove(it)
                    episodesDeleted += 1
                }
            }
            Toast.makeText(context, "Deleted %d episodes and %d files".format(episodesDeleted, filesDeleted), Toast.LENGTH_LONG).show()
        }

        return rootView
    }

    private fun onRefreshComplete(application: EchoApplication, podcast: Podcast, episodes: List<Episode>) {
        val chronologicalEpisodes = podcast.chronologicalEpisodes()

        val episodes = chronologicalEpisodes
                .subList(0, minOf(3, chronologicalEpisodes.size))
                .filter { e -> e.getFile(application as Context).exists() }

        Toast.makeText(application, podcast.title + " refreshed.", Toast.LENGTH_SHORT).show()
    }
}
