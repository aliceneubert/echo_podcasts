package com.zacneubert.echo.utils

import java.text.SimpleDateFormat
import java.util.*

fun formattedDate(date: Date?): String {
    return if (date == null) {
        ""
    } else {
        SimpleDateFormat("MM-dd-yyyy HH:mm").format(date)
    }
}