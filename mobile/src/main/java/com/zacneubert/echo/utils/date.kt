package com.zacneubert.echo.utils

import java.text.SimpleDateFormat
import java.util.*

const val SECOND = 1000
const val MINUTE = 60 * SECOND
const val HOUR = 60 * MINUTE
const val DAY = 24 * HOUR
const val WEEK = 7 * DAY

fun formattedDate(date: Date?): String {
    return if (date == null) {
        ""
    } else {
        SimpleDateFormat("MM-dd-yyyy").format(date)
    }
}

fun formattedDatetime(date: Date?): String {
    return if (date == null) {
        ""
    } else {
        SimpleDateFormat("MM-dd-yyyy HH:mm").format(date)
    }
}