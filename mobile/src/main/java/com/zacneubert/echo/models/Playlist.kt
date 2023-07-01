package com.zacneubert.echo.models

import android.os.Parcel
import android.os.Parcelable
import android.widget.Toast
import com.bumptech.glide.Glide
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.ParsingFeedException
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
import java.net.URLConnection

@Entity class Playlist() : Parcelable {
    @Id var id: Long = 0

    var title: String = ""
    var description: String = ""

    @Backlink
    lateinit var playlistEpisodes: ToMany<PlaylistEpisode>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString() ?: ""
        description = parcel.readString() ?: ""
    }

    fun topNChronological(n: Int): List<Episode> {
        val chronologicalEpisodes = this.chronologicalEpisodes()
        return chronologicalEpisodes.subList(0, minOf(n, chronologicalEpisodes.size))
    }

    fun chronologicalEpisodes(): List<Episode> {
        return episodeList().sortedByDescending { e -> e.publishDate }
    }

    fun episodeList() : List<Episode> {
        return playlistEpisodes
                .filter { !it.episode.isNull && !it.episode.getTarget().podcast.isNull }
                .sortedBy { it.position }.map{pe -> pe.episode.target}
    }

    fun unplayedEpisodes() : List<Episode> {
        return episodeList().filter { !it.played }
    }

    fun nextUnplayed() : Episode? {
        return unplayedEpisodes().firstOrNull()
    }

    fun artUrl(): String {
        val nextUp = this.nextUnplayed()
        if(nextUp != null) {
            return if (nextUp.artUrl != null && nextUp.artUrl.isNotEmpty()) {
                nextUp.artUrl
            } else {
                nextUp.podcast.target.artUri
            }
        }
        return ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }
}