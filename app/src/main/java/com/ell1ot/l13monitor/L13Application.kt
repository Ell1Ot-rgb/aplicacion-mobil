package com.ell1ot.l13monitor

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ell1ot.l13monitor.workers.CyclePollerWorker
import com.ell1ot.l13monitor.workers.HealthCheckWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Application root: Hilt container + WorkManager with Hilt-enabled workers. */
@HiltAndroidApp
class L13Application : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()

        try {
            val wm = WorkManager.getInstance(this)
            wm.enqueueUniquePeriodicWork(
                HealthCheckWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<HealthCheckWorker>(15, TimeUnit.MINUTES).build(),
            )
            // WorkManager min periodic = 15min; the in-app poller/Refresh covers tighter cadences.
            wm.enqueueUniquePeriodicWork(
                CyclePollerWorker.UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<CyclePollerWorker>(15, TimeUnit.MINUTES).build(),
            )
        } catch (e: Exception) {
            android.util.Log.e("L13Application", "WorkManager periodic enqueue caught exception", e)
        }
    }
}
