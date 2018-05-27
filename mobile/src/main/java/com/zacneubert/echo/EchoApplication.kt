package com.zacneubert.echo

import android.app.Application
import com.zacneubert.echo.models.MyObjectBox
import io.objectbox.BoxStore

class EchoApplication : Application() {
    lateinit var boxStore: BoxStore

    override fun onCreate() {
        super.onCreate()

        boxStore = MyObjectBox.builder().androidContext(this).build()
    }
}