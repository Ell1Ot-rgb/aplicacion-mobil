package com.ell1ot.l13monitor.core.transport

import com.ell1ot.l13monitor.core.commands.CommandResult
import com.ell1ot.l13monitor.core.commands.L13Command
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * TODO(v2): implement when the L13 C++ server exposes a WebSocket endpoint.
 * For now it is a compile-time-visible stub so the Transport contract is sealed.
 */
@Singleton
class WebSocketTransport @Inject constructor() : Transport {
    override suspend fun send(command: L13Command): CommandResult =
        CommandResult.Failure("WebSocketTransport not implemented yet (L13 v2)")

    override fun telemetry(): Flow<TelemetryEvent> = emptyFlow()
}
