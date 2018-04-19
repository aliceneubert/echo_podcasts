package com.zacneubert.echo.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN
import com.zacneubert.echo.player.MediaPlayerService.Companion.mediaControls
import com.zacneubert.echo.player.MediaPlayerService.Companion.playbackState

class MediaIntentReceiver : BroadcastReceiver() {
    companion object {
        const val MEDIA_INTENT_KEY_CODE: String = "MEDIA_INTENT_KEY_CODE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.apply {
            if (intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) != null) {
                val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) as KeyEvent
                if (event.action == ACTION_DOWN) {
                    context?.apply {
                        processKeyCode(context, event.keyCode)
                    }
                }
            } else if (intent.hasExtra(MEDIA_INTENT_KEY_CODE)) {
                val keyCode = intent.getIntExtra(MEDIA_INTENT_KEY_CODE, -1)
                context?.apply {
                    processKeyCode(context, keyCode)
                }
            } else {
                var i = 0
                i++
            }
        }
    }

    private fun processKeyCode(context: Context, keyCode: Int) {
        mediaControls(context)?.apply {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> play()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> pause()
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    when(playbackState(context)?.state) {
                        PlaybackStateCompat.STATE_PLAYING -> pause()
                        PlaybackStateCompat.STATE_PAUSED -> play()
                    }
                }

                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                KeyEvent.KEYCODE_MEDIA_NEXT -> skipToNext()

                KeyEvent.KEYCODE_MEDIA_REWIND,
                KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> skipToPrevious()
            }
        }
    }
}