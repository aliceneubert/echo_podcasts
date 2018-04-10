package com.zacneubert.echo.models

import android.net.Uri
import java.io.File
import java.io.Serializable

public class Episode(podcast: Podcast, file: File) : Serializable {
    val title: String = file.name
    val podcast: Podcast = podcast
    val file: File = file

    fun getUri() : Uri {
        return Uri.fromFile(file)
    }

    fun getStopTimeKey() : String {
        return file.absolutePath + "__" + podcast.title + "__STOPPED_TIME"
    }
}