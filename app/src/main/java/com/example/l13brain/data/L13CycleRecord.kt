package com.example.l13brain.data

/**
 * Data model for persisted L13 cycle records, mirroring the l13_cycles table from the backend.
 */
data class L13CycleRecord(
    val cycle: Long,
    val timestamp: Long,
    val status: String,
    val stability: Double,
    val topoLoss: Double,
    val betti0L13: Double,
    val betti1L13: Double,
    val vsaSimilarity: Double,
    val pcaVariancePct: Double,
    val nNodes: Int,
    val nEdges: Int,
    val similarityThreshold: Double = 0.80,
    val topoEventType: String = "NONE",
    val wolframSteps: Int = 0,
    val mlCycles: Int = 0
)
