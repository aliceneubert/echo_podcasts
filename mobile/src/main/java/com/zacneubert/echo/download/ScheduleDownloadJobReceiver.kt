package com.zacneubert.echo.download

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import android.util.Log

class ScheduleDownloadJobReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("EchoBoot", "OnReceive Called")
        if (context != null) scheduleEveryHour(context)
    }
}

class StartMassDownloadSetupJobService : JobService() {
    override fun onStartJob(params: JobParameters): Boolean {
        ContextCompat.startForegroundService(applicationContext, MassDownloadSetupService.ignitionIntent(applicationContext))
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return true
    }

    companion object {
        private val TAG = "StartMassDownloadSetupJobService"
    }

}

val SECOND = 1000L
val MINUTE = SECOND * 60
val HOUR = MINUTE * 60

fun scheduleEveryHour(context: Context) {
    Log.i("EchoBoot", "Schedule every 30 called")
    val serviceComponent: ComponentName = ComponentName(context, StartMassDownloadSetupJobService::class.java)
    Log.i("EchoBoot", "a")
    val builder: JobInfo.Builder = JobInfo.Builder(37, serviceComponent)
    Log.i("EchoBoot", "b")
    builder.setPeriodic(1 * HOUR)
    Log.i("EchoBoot", "c")
    val jobScheduler = context.getSystemService(JobScheduler::class.java)
    Log.i("EchoBoot", "d")
    jobScheduler.schedule(builder.build())
    Log.i("EchoBoot", "Schedule every 30 completed")
}
