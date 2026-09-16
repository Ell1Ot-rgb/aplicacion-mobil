package com.ell1ot.l13monitor.domain.usecase

import com.ell1ot.l13monitor.data.local.db.CycleEntity
import com.ell1ot.l13monitor.data.repository.L13Repository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class StreamCyclesUseCase @Inject constructor(private val repo: L13Repository) {
    operator fun invoke(): Flow<List<CycleEntity>> = repo.cycleHistory()
}
