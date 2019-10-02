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
public class PlaylistEpisode() : Parcelable {
    @Id
    var id: Long = 0

    var position: Long = 0
    lateinit var episode: ToOne<Episode>
    lateinit var playlist: ToOne<Playlist>

    constructor(parcel: Parcel) : this() {
        id = parcel.readLong()
        position = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(position)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlaylistEpisode> {
        override fun createFromParcel(parcel: Parcel): PlaylistEpisode {
            return PlaylistEpisode(parcel)
        }

        override fun newArray(size: Int): Array<PlaylistEpisode?> {
            return arrayOfNulls(size)
        }
    }
}