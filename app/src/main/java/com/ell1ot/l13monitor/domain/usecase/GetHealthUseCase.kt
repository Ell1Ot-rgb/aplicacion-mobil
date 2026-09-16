package com.ell1ot.l13monitor.domain.usecase

import com.ell1ot.l13monitor.data.repository.L13Repository
import javax.inject.Inject

class GetHealthUseCase @Inject constructor(private val repo: L13Repository) {
    suspend operator fun invoke(): Result<String?> = repo.refreshHealth()
}
