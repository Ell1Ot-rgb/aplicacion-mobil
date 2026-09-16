package com.ell1ot.l13monitor.core.transport

import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.core.commands.CommandResult
import kotlinx.coroutines.flow.Flow

/** Abstraction over the wire. RestTransport today, WebSocket when L13 C++ supports it. */
data class TelemetryEvent(
    val cycle: Int,
    val status: String,
    val stability: Double?,
    val timestampMillis: Long,
)

interface Transport {
    /** Send a command and return its ACK/result. Suspending; never throws. */
    suspend fun send(command: L13Command): CommandResult

    /** Live telemetry stream (from polling or push). */
    fun telemetry(): Flow<TelemetryEvent>
}
