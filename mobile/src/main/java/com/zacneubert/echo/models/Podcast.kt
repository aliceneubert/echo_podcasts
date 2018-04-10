package com.zacneubert.echo.models

import java.io.File
import java.io.Serializable

public class Podcast(val folder: File) : Serializable {
    val title: String = folder.name
    val description: String = "Description"

    val episodes: List<Episode> = folder.listFiles().map { file ->
        Episode(this, file)
    }
}