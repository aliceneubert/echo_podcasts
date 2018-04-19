package com.zacneubert.echo.helpers

class TimeMillis {
    companion object {
        val SECOND: Long = 1000
        val MINUTE: Long = SECOND * 60
        val HOUR: Long = MINUTE * 60
        val DAY: Long = HOUR * 24

        fun composite(seconds: Long = 0, minutes: Long = 0, hours: Long = 0, days: Long = 0) : Long {
            return SECOND * seconds + MINUTE * minutes + HOUR * hours + DAY * days
        }
    }
}