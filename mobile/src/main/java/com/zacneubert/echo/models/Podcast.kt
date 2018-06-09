package com.zacneubert.echo.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.io.File
import java.io.Serializable

@Entity class Podcast() : Parcelable {
    @Id var id: Long = 0

    var title: String = ""
    var description: String = ""
    var artist: String = ""
    var feedUrl: String = ""
    var artUri: String = ""

    @Backlink
    lateinit var episodes: List<Episode>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        title = parcel.readString()
        description = parcel.readString()
        artist = parcel.readString()
        feedUrl = parcel.readString()
        artUri = parcel.readString()
        episodes = parcel.createTypedArrayList(Episode)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(artist)
        parcel.writeString(feedUrl)
        parcel.writeString(artUri)
        parcel.writeTypedList(episodes)
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