package com.zacneubert.echo.download

import com.zacneubert.echo.utils.KB
import com.zacneubert.echo.utils.SECOND
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


@Throws(IOException::class)
fun downloadFile(url: String, file: File, updateProgress: ((Long, Long) -> Unit)?) {
    var url = url
    var base: URL
    var next: URL
    var resourceUrl: URL
    var connection: HttpURLConnection
    var location: String

    while (true) {
        val secureUrl = url.replace("http://", "https://")
        resourceUrl = URL(secureUrl)
        connection = resourceUrl.openConnection() as HttpURLConnection

        connection.connectTimeout = 15 * SECOND
        connection.readTimeout = 15 * SECOND
        connection.instanceFollowRedirects = false   // Make the logic below easier to detect redirections
        connection.setRequestProperty("User-Agent", "Mozilla/5.0...")

        var status = connection.responseCode
        if (status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_MOVED_TEMP) {
            location = connection.getHeaderField("Location")
            base = URL(url)
            next = URL(base, location)  // Deal with relative URLs
            url = next.toExternalForm()
            continue
        } else if (status == HttpURLConnection.HTTP_NOT_FOUND) {
            throw Exception("URL not found: $url")
        }
        break
    }

    val filesize = connection.getContentLength()
    val input = BufferedInputStream(connection.getInputStream(), 32 * KB)
    val outstream = FileOutputStream(file.absolutePath)
    val data = ByteArray(8 * KB)
    var total = 0

    var count: Int

    var lastUpdate = 0L

    count = input.read(data)
    while (count != -1) {
        total += count
        if (updateProgress != null) {
            if (System.currentTimeMillis() - lastUpdate > SECOND * .25) {
                updateProgress(total.toLong(), filesize.toLong())
                lastUpdate = System.currentTimeMillis()
            }
        }
        outstream.write(data, 0, count)
        count = input.read(data)
    }

    outstream.flush()
    outstream.close()
    input.close()

    if (updateProgress != null) {
        updateProgress(filesize.toLong(), filesize.toLong())
    }
}