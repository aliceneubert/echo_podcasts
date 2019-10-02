package com.zacneubert.echo.settings

import android.content.Context
import android.content.SharedPreferences
import org.jetbrains.anko.defaultSharedPreferences

class VolumeSetting : Setting<Long>() {
    companion object {
        val KEY = "INTERNAL_VOLUME_LEVEL"

        fun get(context: Context) : Long {
            return context.defaultSharedPreferences.getLong(KEY, 50L)
        }

        fun set(context: Context, value : Long) : Boolean {
            return context.defaultSharedPreferences.edit().putLong(KEY, value).commit()
        }
    }
}