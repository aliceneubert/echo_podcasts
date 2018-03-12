package com.zacneubert.echo.models

class Podcast(i: Int) {
    val title: String = "%d A Podcast".format(i)
    val description: String = "%d What a great podcast".format(i)
}