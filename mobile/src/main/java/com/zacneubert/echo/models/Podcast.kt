package com.zacneubert.echo.models

import android.os.Parcel
import android.os.Parcelable
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import com.zacneubert.echo.EchoApplication
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.net.URL

@Entity class Podcast() : Parcelable {
    @Id var id: Long = 0

    var title: String = ""
    var description: String = ""
    var artist: String = ""
    var feedUrl: String = ""
    var artUri: String = ""

    @Backlink
    lateinit var episodes: ToMany<Episode>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString()
        description = parcel.readString()
        artist = parcel.readString()
        feedUrl = parcel.readString()
        artUri = parcel.readString()
    }

    fun getFeed(application: EchoApplication): SyndFeed? {
        val feedUrl = URL(feedUrl)

        val syndFeedInput = SyndFeedInput()
        val feed = syndFeedInput.build(XmlReader(feedUrl))

        updateFromFeed(feed, application)

        return feed
    }

    private fun updateFromFeed(feed: SyndFeed, application: EchoApplication) {
        title = feed.title.trim()
        description = feed.description.trim()
        application.podcastBox()!!.put(this)
    }

    fun refreshEpisodeList(application: EchoApplication, onComplete: ((episodes: List<Episode>) -> Unit)?) {
        if(id == 0L) {
            return
        }

        doAsync {
            val episodeBox = application.episodeBox()

            episodes.forEach { e -> episodeBox!!.remove(e) }

            val podcast = Podcast@this.weakRef.get()!!
            getFeed(application)!!.entries.forEach {
                val episode = Episode(Podcast@ this.weakRef.get()!!, it)
                podcast.episodes.add(episode)
            }
            application.podcastBox()!!.put(podcast)

            uiThread {
                if (onComplete != null) {
                    onComplete(podcast.episodes)
                }
            }
        }
    }

    fun chronologicalEpisodes(): List<Episode> {
        return episodes.sortedByDescending { e -> e.publishDate }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(artist)
        parcel.writeString(feedUrl)
        parcel.writeString(artUri)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Podcast> {
        override fun createFromParcel(parcel: Parcel): Podcast {
            return Podcast(parcel)
        }

        override fun newArray(size: Int): Array<Podcast?> {
            return arrayOfNulls(size)
        }
    }
}