package com.ell1ot.l13monitor.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ell1ot.l13monitor.data.local.dao.CycleDao
import com.ell1ot.l13monitor.data.local.db.CycleEntity
import com.ell1ot.l13monitor.data.remote.L13ApiService
import com.ell1ot.l13monitor.data.remote.dto.InferRequest
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Reads /infer (query_state) at the OS-allowed cadence and persists to Room. */
@HiltWorker
class CyclePollerWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val api: L13ApiService,
    private val dao: CycleDao,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val r = api.infer(InferRequest(command = "query_state"))
            dao.upsert(
                CycleEntity(
                    cycle = r.cycle,
                    status = r.status,
                    stability = r.stability,
                    topoLoss = r.topoLoss,
                    betti0 = r.betti0L13,
                    betti1 = r.betti1L13,
                    nNodes = r.nNodes,
                    nEdges = r.nEdges,
                    tsMillis = System.currentTimeMillis(),
                ),
            )
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object { const val UNIQUE_NAME = "l13-cycle-poller" }
}
