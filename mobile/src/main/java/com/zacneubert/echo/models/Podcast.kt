package com.zacneubert.echo.models

import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.io.File
import java.io.Serializable

@Entity class Podcast : Serializable {
    @Id var id: Long = 0

    val title: String = ""
    val description: String = ""
    val feedUrl: String = ""

    @Backlink
    lateinit var episodes: List<Episode>
}