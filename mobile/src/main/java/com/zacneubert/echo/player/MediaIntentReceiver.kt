package com.zacneubert.echo.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_DOWN

class MediaIntentReceiver : BroadcastReceiver() {
    companion object {
        val MEDIA_INTENT_KEY_CODE: String = "MEDIA_INTENT_KEY_CODE"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.apply {
            if (intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) != null) {
                val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) as KeyEvent
                if (event.action == ACTION_DOWN) {
                    processKeyCode(event.keyCode)
                }
            } else if (intent.hasExtra(MEDIA_INTENT_KEY_CODE)) {
                val keyCode = intent.getIntExtra(MEDIA_INTENT_KEY_CODE, -1)
                processKeyCode(keyCode)
            } else {
                var i = 0
                i++
            }
        }
    }

    private fun processKeyCode(keyCode: Int) {
        MediaPlayerService.mediaPlayerService?.apply {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> start()
                KeyEvent.KEYCODE_MEDIA_PAUSE -> pause()
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> togglePlaying()

                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD,
                KeyEvent.KEYCODE_MEDIA_NEXT -> skipForward()

                KeyEvent.KEYCODE_MEDIA_REWIND,
                KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD,
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> skipBackward()
            }
        }
    }
}