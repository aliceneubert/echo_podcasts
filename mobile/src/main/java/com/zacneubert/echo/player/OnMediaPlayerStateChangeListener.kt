package com.zacneubert.echo.player

import android.media.MediaPlayer
import com.zacneubert.echo.models.Episode

interface OnMediaStateChangeListener {
    fun onMediaStateChange(mediaPlayer: MediaPlayer, episode: Episode)
}