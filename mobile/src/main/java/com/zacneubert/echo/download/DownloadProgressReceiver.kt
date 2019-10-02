package com.zacneubert.echo.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.zacneubert.echo.download.DownloadService.Companion.BROADCAST_ACTION
import com.zacneubert.echo.download.DownloadService.Companion.BROADCAST_EPISODE_ID
import com.zacneubert.echo.download.DownloadService.Companion.BROADCAST_PROGRESS_PERCENT

class DownloadProgressReceiver(private val onProgress: (Long, Double) -> Unit) : BroadcastReceiver() {
    companion object {
        fun getIntentFilter(): IntentFilter {
            val intentFilter = IntentFilter()
            intentFilter.addAction(BROADCAST_ACTION)
            return intentFilter
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.apply {
            val episodeId = intent.getLongExtra(BROADCAST_EPISODE_ID, 0L)
            val progressPercent = intent.getDoubleExtra(BROADCAST_PROGRESS_PERCENT, 0.0)
            if(episodeId != 0L) {
                onProgress(episodeId, progressPercent)
            }
        }
    }
}
