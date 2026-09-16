package com.ell1ot.l13monitor.domain.usecase

import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.data.repository.L13Repository
import javax.inject.Inject

class SendCommandUseCase @Inject constructor(private val repo: L13Repository) {
    operator fun invoke(command: L13Command) = repo.sendCommand(command)
}
