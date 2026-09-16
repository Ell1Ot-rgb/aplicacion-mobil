package com.ell1ot.l13monitor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ell1ot.l13monitor.data.local.dao.HealthDao
import com.ell1ot.l13monitor.data.local.db.HealthSnapshotEntity
import com.ell1ot.l13monitor.data.remote.L13ApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Periodic health = cheap GET /health stored in Room. WorkManager imposes 15min minimum. */
@HiltWorker
class HealthCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: L13ApiService,
    private val dao: HealthDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val h = api.health()
            dao.insert(HealthSnapshotEntity(status = h.status, tsMillis = System.currentTimeMillis()))
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object { const val UNIQUE_NAME = "l13-health-check" }
}
