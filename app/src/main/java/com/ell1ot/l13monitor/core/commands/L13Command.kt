package com.ell1ot.l13monitor.core.commands

import java.util.UUID

/** Sealed command vocabulary toward the L13 hypergraph server. */
sealed class L13Command {
    abstract val cmdId: String
    abstract val createdAtMillis: Long

    data class TriggerCycle(
        val nTicks: Int = 50,
        override val cmdId: String = UUID.randomUUID().toString(),
        override val createdAtMillis: Long = System.currentTimeMillis(),
    ) : L13Command()

    data class IngestBetti(
        val betti0: Int,
        val betti1: Int,
        val intensity: Float = 1.0f,
        override val cmdId: String = UUID.randomUUID().toString(),
        override val createdAtMillis: Long = System.currentTimeMillis(),
    ) : L13Command()

    data object QueryState : L13Command() {
        override val cmdId: String get() = "query-state"
        override val createdAtMillis: Long get() = System.currentTimeMillis()
    }

    data class CalibrateTau(
        val tau: Float,
        override val cmdId: String = UUID.randomUUID().toString(),
        override val createdAtMillis: Long = System.currentTimeMillis(),
    ) : L13Command()

    data class InjectVector(
        val thoughtVector: List<Float>,
        val conceptVector: List<Float>,
        override val cmdId: String = UUID.randomUUID().toString(),
        override val createdAtMillis: Long = System.currentTimeMillis(),
    ) : L13Command()

    data class ResetGraph(
        val confirm: Boolean = false,
        override val cmdId: String = UUID.randomUUID().toString(),
        override val createdAtMillis: Long = System.currentTimeMillis(),
    ) : L13Command()
}
