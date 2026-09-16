package com.ell1ot.l13monitor.domain.model

data class Cycle(
    val id: Long,
    val cycle: Int?,
    val status: String?,
    val stability: Double?,
    val topoLoss: Double?,
    val tsMillis: Long,
)

data class BettiEvent(val betti0: Int, val betti1: Int, val tsMillis: Long)

data class HypergraphState(
    val nNodes: Int,
    val nEdges: Int,
    val fusedEmbedding: List<Double>? = null,
)

data class DynoInfo(val type: String, val quantity: Int, val size: String)

data class SpaceInfo(val id: String, val state: String, val lastSeenMillis: Long?)
