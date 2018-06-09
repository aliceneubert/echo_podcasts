package com.zacneubert.echo

import android.app.Application
import com.zacneubert.echo.models.Episode
import com.zacneubert.echo.models.MyObjectBox
import com.zacneubert.echo.models.Podcast
import io.objectbox.Box
import io.objectbox.BoxStore

class EchoApplication : Application() {
    lateinit var boxStore: BoxStore

    override fun onCreate() {
        super.onCreate()

        boxStore = MyObjectBox.builder().androidContext(this).build()
    }

    fun episodeBox(): Box<Episode>? {
        return boxStore.boxFor(Episode::class.java)
    }

    fun podcastBox(): Box<Podcast>? {
        return boxStore.boxFor(Podcast::class.java)
    }
}