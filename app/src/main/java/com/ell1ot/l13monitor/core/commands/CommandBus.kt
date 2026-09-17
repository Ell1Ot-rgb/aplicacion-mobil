package com.ell1ot.l13monitor.core.commands

import com.ell1ot.l13monitor.data.local.dao.CommandLogDao
import com.ell1ot.l13monitor.data.local.db.CommandLogEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

/** FIFO command pump with exponential backoff + ACK persistence. */
@Singleton
class CommandBus @Inject constructor(
    private val transport: com.ell1ot.l13monitor.core.transport.Transport,
    private val dao: CommandLogDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _acks = MutableSharedFlow<CommandLogEntity>(extraBufferCapacity = 64)
    val acks: SharedFlow<CommandLogEntity> = _acks

    // v3 fix (audit #2023): real FIFO. Single channel consumed sequentially; the
    // old scope.launch-per-command let retries of cmd A interleave with cmd B,
    // so ACK order was NOT the FIFO order the class comment claimed.
    private val queue = Channel<L13Command>(capacity = 64)

    init {
        scope.launch {
            for (command in queue) processCommand(command)
        }
    }

    /** Enqueue into the FIFO pump; trySend applies backpressure when full. */
    fun enqueue(command: L13Command) {
        queue.trySend(command)
    }

    private suspend fun processCommand(command: L13Command) {
            val log = CommandLogEntity(
                cmdId = command.cmdId,
                kind = command::class.simpleName ?: "unknown",
                payloadJson = command.toPayloadString(),
                status = "PENDING",
                attempt = 0,
                createdAtMillis = command.createdAtMillis,
                settledAtMillis = 0L,
            )
            dao.upsert(log)

            var attempt = 0
            var backoffMs = 1_000L
            var result: CommandResult
            do {
                if (attempt > 0) delay(backoffMs)
                attempt++
                result = transport.send(command)
                when (result) {
                    is CommandResult.Pending -> {
                        if (attempt < MAX_ATTEMPTS) backoffMs = (backoffMs * 2).coerceAtMost(MAX_BACKOFF_MS)
                        dao.upsert(log.copy(status = "RETRYING", attempt = attempt))
                    }
                    else -> Unit
                }
            } while (result is CommandResult.Pending && attempt < MAX_ATTEMPTS)

            val finalStatus = when (result) {
                is CommandResult.Success -> "ACK"
                is CommandResult.Pending -> "TIMED_OUT"
                is CommandResult.Failure -> "FAILED"
            }
            dao.upsert(
                log.copy(
                    status = finalStatus,
                    attempt = attempt,
                    settledAtMillis = System.currentTimeMillis(),
                ),
            )
            _acks.emit(
                CommandLogEntity(
                    cmdId = command.cmdId,
                    kind = log.kind,
                    payloadJson = log.payloadJson,
                    status = finalStatus,
                    attempt = attempt,
                    createdAtMillis = log.createdAtMillis,
                    settledAtMillis = System.currentTimeMillis(),
                ),
            )
    }

    private fun L13Command.toPayloadString(): String = when (this) {
        is L13Command.TriggerCycle -> "ticks=$nTicks"
        is L13Command.IngestBetti -> "b0=$betti0 b1=$betti1 intensity=$intensity"
        is L13Command.QueryState -> "query"
        is L13Command.CalibrateTau -> "tau=$tau"
        is L13Command.InjectVector -> "thought[${thoughtVector.size}] concept[${conceptVector.size}]"
        is L13Command.ResetGraph -> "confirm=$confirm"
    }

    private companion object {
        const val MAX_ATTEMPTS = 4
        const val MAX_BACKOFF_MS = 30_000L
    }
}
