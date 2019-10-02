package com.zacneubert.echo.models

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.rometools.rome.feed.synd.SyndEntry
import com.zacneubert.echo.EchoApplication
import com.zacneubert.echo.download.DownloadService
import com.zacneubert.echo.utils.formattedDate
import com.zacneubert.echo.utils.formattedDatetime
import com.zacneubert.echo.utils.scrubFilename
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.io.File
import java.util.*
import kotlin.system.exitProcess

@Entity
public class Episode() : Parcelable {
    @Id
    var id: Long = 0

    var uid: String = ""
    var title: String = ""
    var streamingUrl: String = ""
    var lastStopTime: Long = 0
    var artUrl: String = ""
    var description: String = ""
    var publishDate: Date? = null
    var downloadDate: Date? = null
    var played: Boolean = false

    lateinit var podcast: ToOne<Podcast>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        uid = parcel.readString()
        title = parcel.readString()
        streamingUrl = parcel.readString()
        lastStopTime = parcel.readLong()
        artUrl = parcel.readString()
        description = parcel.readString()
        played = parcel.readByte() != 0.toByte()
    }

    constructor(podcast: Podcast, entry: SyndEntry) : this() {
        this.podcast.target = podcast

        title = entry.title.toString()
        entry.description?.apply {
            description = entry.description.value.trim()
        }

        entry.enclosures.firstOrNull()?.apply {
            streamingUrl = this.url
        }

        if (entry.foreignMarkup.isNotEmpty()) {
            entry.foreignMarkup.firstOrNull { fm -> fm.name == "image" }?.apply {
                this.attributes.firstOrNull { attr -> attr.name == "href" }?.apply {
                    this@Episode.artUrl = this.value
                }
            }
        }

        uid = entry.uri
        publishDate = entry.publishedDate
    }

    fun isTopN(n: Int) : Boolean {
        return this.podcast.getTarget()
                .topNChronological(3)
                .map { e -> e.id }
                .contains(this.id)
    }

    fun publishDateOrEpoch(): Date {
        return publishDate ?: Date(0)
    }

    fun relativePath(): String {
        return scrubFilename(String.format("files/%s-%s", this.podcast.getTarget().title, this.title))
    }

    fun downloadFile(application: EchoApplication) {
        if(!getFile(application).exists()) {
            ContextCompat.startForegroundService(application, DownloadService.ignitionIntent(application, this))
            this.downloadDate = Date()
        }
    }

    fun deleteFile(context: Context) {
        val file = getFile(context)
        if(file.exists()) file.delete()
        this.downloadDate = null
    }

    fun getFile(context: Context): File {
        return context.dataDir.resolve(relativePath())
    }

    fun getStreamingUri(context: Context): Uri {
        return Uri.parse(streamingUrl)
    }

    fun getUri(context: Context): Uri {
        val file = getFile(context)

        return if (file.exists())
                Uri.fromFile(file)
            else
                Uri.parse(streamingUrl.replace("http://", "https://"))
    }

    fun formattedDatetime(): String {
        return formattedDatetime(this.publishDate)
    }

    fun formattedDate(): String {
        return formattedDate(this.publishDate)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(uid)
        parcel.writeString(title)
        parcel.writeString(streamingUrl)
        parcel.writeLong(lastStopTime)
        parcel.writeString(artUrl)
        parcel.writeString(description)
        parcel.writeByte(if (played) 1 else 0)
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