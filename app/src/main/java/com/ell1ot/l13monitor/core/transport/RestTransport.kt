package com.ell1ot.l13monitor.core.transport

import com.ell1ot.l13monitor.core.commands.CommandResult
import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.data.remote.L13ApiService
import com.ell1ot.l13monitor.data.remote.dto.InferRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

/** REST transport against the L13 cpp server (`/health`, `/infer`). */
@Singleton
class RestTransport @Inject constructor(
    private val api: L13ApiService,
) : Transport {

    override suspend fun send(command: L13Command): CommandResult {
        return try {
            val request = command.toRequest()
            val response = api.infer(request)
            CommandResult.Success(ackId = response.cycle?.toString(), message = response.status ?: "ok")
        } catch (e: HttpException) {
            if (e.code() == 401) CommandResult.Failure("unauthorized (401) — token faltante o inválido")
            else if (e.code() >= 500) CommandResult.Pending(retryAfterMs = 5_000, reason = "server ${e.code()}")
            else CommandResult.Failure("http ${e.code()}")
        } catch (e: IOException) {
            CommandResult.Pending(retryAfterMs = 3_000, reason = "network: ${e.message ?: "io"}")
        } catch (e: Exception) {
            CommandResult.Failure("unexpected: ${e.message ?: e::class.simpleName}")
        }
    }

    override fun telemetry(): Flow<TelemetryEvent> = flow {
        while (true) {
            try {
                val h = api.health()
                emit(
                    TelemetryEvent(
                        cycle = -1,
                        status = h.status ?: "unknown",
                        stability = null,
                        timestampMillis = System.currentTimeMillis(),
                    ),
                )
            } catch (_: Exception) { /* swallow; pollers track errors elsewhere */ }
            delay(30_000)
        }
    }

    private fun L13Command.toRequest(): InferRequest = when (this) {
        is L13Command.TriggerCycle -> InferRequest(command = "trigger_cycle", ticks = nTicks)
        is L13Command.IngestBetti -> InferRequest(
            command = "ingest_betti", betti0 = betti0, betti1 = betti1, intensity = intensity,
        )
        is L13Command.QueryState -> InferRequest(command = "query_state")
        is L13Command.CalibrateTau -> InferRequest(command = "calibrate_tau", tau = tau)
        is L13Command.InjectVector -> InferRequest(
            command = "inject_vector",
            thoughtVector = thoughtVector,
            conceptVector = conceptVector,
        )
        is L13Command.ResetGraph -> InferRequest(command = "reset_graph", confirm = confirm)
    }
}
