package com.ell1ot.l13monitor.domain.usecase

import com.ell1ot.l13monitor.data.remote.DynoFormationDto
import com.ell1ot.l13monitor.data.repository.HerokuRepository
import javax.inject.Inject

class MonitorDynoUseCase @Inject constructor(private val repo: HerokuRepository) {
    suspend operator fun invoke(): Result<List<DynoFormationDto>> = repo.formation()
}
