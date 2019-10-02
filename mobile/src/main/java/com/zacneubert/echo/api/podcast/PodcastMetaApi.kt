package com.zacneubert.echo.api.itunes

import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

interface PodcastMetaApi {
    companion object {
        fun getLastModified(path: String): Long {
            var lastModified = 0L
            var urlConnection: HttpURLConnection? = null
            val securePath = path.replace("http://", "https://")
            System.setProperty("http.keepAlive", "false")
            try {
                val url = URL(securePath)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "HEAD"
                urlConnection.inputStream.close()
                lastModified = urlConnection.lastModified
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
            }
            return lastModified
        }
    }
}
