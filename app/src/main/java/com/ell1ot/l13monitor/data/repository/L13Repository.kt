package com.ell1ot.l13monitor.data.repository

import com.ell1ot.l13monitor.core.commands.CommandBus
import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.data.local.dao.CommandLogDao
import com.ell1ot.l13monitor.data.local.dao.CycleDao
import com.ell1ot.l13monitor.data.local.dao.HealthDao
import com.ell1ot.l13monitor.data.local.db.CycleEntity
import com.ell1ot.l13monitor.data.local.db.HealthSnapshotEntity
import com.ell1ot.l13monitor.data.remote.L13ApiService
import com.ell1ot.l13monitor.data.remote.dto.InferRequest
import com.ell1ot.l13monitor.data.remote.dto.InferResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class L13Repository @Inject constructor(
    private val api: L13ApiService,
    private val cycleDao: CycleDao,
    private val healthDao: HealthDao,
    private val commandLogDao: CommandLogDao,
    private val commandBus: CommandBus,
) {

    suspend fun refreshHealth(): Result<String?> = runCatching { api.health() }
        .onSuccess { h ->
            healthDao.insert(HealthSnapshotEntity(status = h.status, tsMillis = System.currentTimeMillis()))
        }
        .map { it.status }

    suspend fun refreshLastCycle(): Result<InferResponse> = runCatching {
        api.infer(InferRequest(command = "query_state"))
    }.onSuccess { r ->
        cycleDao.upsert(
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
    }

    fun sendCommand(command: L13Command) = commandBus.enqueue(command)

    fun cycleHistory(): Flow<List<CycleEntity>> = cycleDao.observeRecent()
    fun latestHealth(): Flow<HealthSnapshotEntity?> = healthDao.observeLatest()
    fun commandHistory(): Flow<List<com.ell1ot.l13monitor.data.local.db.CommandLogEntity>> =
        commandLogDao.observeRecent()
}
