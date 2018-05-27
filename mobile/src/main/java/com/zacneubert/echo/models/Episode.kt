package com.zacneubert.echo.models

import android.net.Uri
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne
import java.io.File
import java.io.Serializable

@Entity
public class Episode : Serializable {
    @Id var id: Long = 0

    var title: String = ""
    var absolutePath: String = ""
    var streamingUrl: String = ""
    var lastStopTime: Long = 0

    lateinit var podcast: ToOne<Podcast>

    fun getFile() : File {
        return File(absolutePath)
    }

    fun getUri() : Uri {
        return if (absolutePath != "") Uri.fromFile(getFile()) else Uri.parse(streamingUrl)
    }
}