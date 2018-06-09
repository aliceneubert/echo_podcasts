package com.zacneubert.echo.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.rometools.rome.feed.synd.SyndEntry
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.RelationInfo
import io.objectbox.relation.ToOne
import java.io.File
import java.io.Serializable
import java.util.*

@Entity
public class Episode() : Parcelable {
    @Id var id: Long = 0

    var title: String = ""
    var absolutePath: String = ""
    var streamingUrl: String = ""
    var lastStopTime: Long = 0
    var artUrl: String = ""
    var description: String = ""
    var publishDate: Date? = null

    lateinit var podcast: ToOne<Podcast>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString()
        absolutePath = parcel.readString()
        streamingUrl = parcel.readString()
        lastStopTime = parcel.readLong()
        artUrl = parcel.readString()
        description = parcel.readString()
    }

    fun getFile() : File {
        return File(absolutePath)
    }

    fun getUri() : Uri {
        return if (absolutePath != "") Uri.fromFile(getFile()) else Uri.parse(streamingUrl)
    }

    constructor(podcast: Podcast, entry : SyndEntry) : this() {
        this.podcast.target = podcast

        title = entry.title.toString()
        entry.description?.apply {
            description = entry.description.value
        }

        entry.enclosures.firstOrNull()?.apply {
            streamingUrl = this.url
        }
        publishDate = entry.publishedDate
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(absolutePath)
        parcel.writeString(streamingUrl)
        parcel.writeLong(lastStopTime)
        parcel.writeString(artUrl)
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Episode> {
        override fun createFromParcel(parcel: Parcel): Episode {
            return Episode(parcel)
        }

        override fun newArray(size: Int): Array<Episode?> {
            return arrayOfNulls(size)
        }
    }
}