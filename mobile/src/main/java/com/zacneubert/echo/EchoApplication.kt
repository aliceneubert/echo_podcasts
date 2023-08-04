package com.zacneubert.echo

import android.app.Activity
import android.app.Application
import android.app.Service
import androidx.appcompat.app.AppCompatActivity
import com.zacneubert.echo.models.*
import io.objectbox.Box
import io.objectbox.BoxStore

class EchoApplication : Application() {
    companion object {
        fun instance(activity: Activity): EchoApplication {
            return activity.application as EchoApplication
        }

        fun instance(service: Service): EchoApplication {
            return service.application as EchoApplication
        }
    }

    lateinit var boxStore: BoxStore

    override fun onCreate() {
        super.onCreate()

        boxStore = MyObjectBox.builder().androidContext(this).build()
    }

    fun episodeBox(): Box<Episode> {
        return boxStore.boxFor(Episode::class.java)!!
    }

    fun podcastBox(): Box<Podcast> {
        return boxStore.boxFor(Podcast::class.java)!!
    }

    fun playlistBox(): Box<Playlist> {
        return boxStore.boxFor(Playlist::class.java)!!
    }

    fun playlistEpisodeBox(): Box<PlaylistEpisode> {
        return boxStore.boxFor(PlaylistEpisode::class.java)!!
    }

    fun chronologicalEpisodes() : List<Episode> {
        return episodeBox().all.sortedByDescending { it.publishDateOrEpoch().time }
    }

    fun newestUnplayedEpisode() : Episode? {
        return episodeBox().query()
            .equal(Episode_.played, false)
            .orderDesc(Episode_.publishDate)
            .build()
            .findFirst()
    }

    fun chronologicalPodcasts() : List<Podcast> {
        return podcastBox().all.sortedByDescending {
            if (it.episodes.isNotEmpty()) {
                it.chronologicalEpisodes().first().publishDate!!.time
            } else {
                0
            }
        }
    }
}