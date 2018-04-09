package com.zacneubert.echo.helpers

import android.os.Environment
import com.zacneubert.echo.models.Podcast
import java.io.File
import java.io.FilenameFilter

class LegacyPodcastProvider {
    companion object {
        fun getPodcastDirectory(): File {
            var storageDir = Environment.getExternalStorageDirectory().toString()
            return File("$storageDir/GetPodcasts/")
        }
    }
}