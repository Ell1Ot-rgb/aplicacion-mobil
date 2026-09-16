package com.ell1ot.l13monitor.core.commands

/** Outcome of sending one command over the wire. */
sealed interface CommandResult {
    /** Server accepted / executed. [ackId] is a server-provided receipt when available. */
    data class Success(val ackId: String?, val message: String = "OK") : CommandResult

    /** Retryable: server busy, dyno cold-start, network flap. [retryAfterMs] = suggested backoff. */
    data class Pending(val retryAfterMs: Long, val reason: String = "retryable") : CommandResult

    /** Terminal failure: do not retry automatically. */
    data class Failure(val reason: String, val retriable: Boolean = false) : CommandResult
}
