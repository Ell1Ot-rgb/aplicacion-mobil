package com.example.l13brain.model

data class HyperNode(
    val id: String,
    val label: String,
    var energy: Float, // 0.0 to 1.0+
    var x: Float,      // 0.0 to 1.0 (relative canvas coords)
    var y: Float,      // 0.0 to 1.0
    var vx: Float = 0f,
    var vy: Float = 0f,
    val modalState: String = "[] Phi (Nec)",
    var degree: Int = 2,
    var phase: Float = 0f,         // Kuramoto phase theta_i in [0, 2*pi)
    var naturalFreq: Float = 0.15f, // Natural frequency omega_i
    var occupancyDensity: Float = 0.50f,   // Time-integrated residence density in [0.0, 1.0]
    var peakPersistence: Float = 0.50f,    // Peak persistence record in [0.0, 1.0]
    val traceBins: FloatArray = FloatArray(8) { 0.12f } // 8-band vertical energy heatmap trace accumulator
)

data class HyperEdge(
    val id: String,
    val label: String,
    var weight: Float,
    val nodeIds: List<String>,
    val colorHex: Long = 0x3300FF66,
    var phosphorLuminance: Float = 0.85f,   // L_e in [0.05, 1.0] Digital Phosphor luminance (afterglow)
    var participationFreq: Float = 0.50f,   // f_e in [0.0, 1.0] Sliding window participation frequency
    var excitationHits: Int = 12,           // Total energetic hit/excitation count
    var lastActiveTick: Long = 0L,          // Timestamp of last excitation pulse
    var afterglowTrailRadius: Float = 1.0f  // Phosphor dispersion halo multiplier
)

data class ProcessInfo(
    val pid: Int,
    val user: String,
    val cpuPct: Float,
    val memPct: Float,
    val status: String,
    val command: String,
    val isEngineProcess: Boolean = false
)

enum class ViewMode {
    CONVEX_HULL_EULER,
    INCIDENCE_HEATMAP,
    KONIG_BIPARTITE
}

enum class CrtProfile(val label: String) {
    CRT_P31_GREEN("CRT-P31 GREEN"),
    AMBER_P4("AMBER P4"),
    CYBER_CYAN("CYBER CYAN"),
    CLEAN_MODE("CLEAN MODE")
}

data class ShaderConfig(
    val enabled: Boolean = false,
    val scanlineDensity: Float = 0.40f,
    val barrelCurvature: Float = 0.20f,
    val phosphorBloom: Float = 0.40f,
    val chromaticAberration: Float = 0.10f
)


data class PhysicsConfig(
    val coulombRepulsionKr: Float = 120.0f,
    val hookeSpringKa: Float = 0.045f,
    val frictionDampingGamma: Float = 0.88f,
    val isPhysicsRunning: Boolean = true
)

data class TelemetryState(
    val cpuLoad: Float = 68.4f,
    val ramAllocatedGb: Float = 8.0f,
    val ramTotalGb: Float = 16.0f,
    val entropyShannon: Float = 1.4289f,
    val ticksPerSec: Float = 30.0f,
    val totalTicks: Long = 89421L,
    val socketPacketsPerSec: Int = 4892,
    val wssLatencyMs: Int = 12,
    val wssConnected: Boolean = true,
    val convergencePct: Float = 98.4f,
    val activeNodesCount: Int = 18,
    val activeEdgesCount: Int = 7,
    val cpuHistory: List<Float> = listOf(35f, 42f, 28f, 55f, 68f, 45f, 30f, 62f, 75f, 50f, 40f, 68f),
    val kuramotoOrderR: Float = 0.944f,
    val autopoieticStage: String = "ETAPA III: Sincronía Hebbiana (R -> 0.95)",
    val isPostSumShock: Boolean = false,
    val lastSumTick: Long = 0L,
    val emergentNodesCount: Int = 1,
    val stateHash: String = "bba89ef74c83d8b5",
    val parentHash: String? = null,
    val lastEventType: String = "GENESIS",
    val sensoryEdgeWeight: Float = 1.15f
)

data class ReplLogEntry(
    val id: Long,
    val isCommand: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
