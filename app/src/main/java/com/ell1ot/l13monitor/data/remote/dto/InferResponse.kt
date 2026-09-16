package com.ell1ot.l13monitor.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Mirrors the 25-field response documented for POST /api/v1/infer. */
@Serializable
data class InferResponse(
    val status: String? = null,
    val cycle: Int? = null,
    @SerialName("n_nodes") val nNodes: Int? = null,
    @SerialName("n_edges") val nEdges: Int? = null,
    @SerialName("similarity_threshold") val similarityThreshold: Double? = null,
    @SerialName("pca_fitted") val pcaFitted: Boolean? = null,
    @SerialName("pca_n_seen") val pcaNSeen: Int? = null,
    @SerialName("pca_variance_pct") val pcaVariancePct: Double? = null,
    @SerialName("topo_loss") val topoLoss: Double? = null,
    @SerialName("betti_0_l13") val betti0L13: Int? = null,
    @SerialName("betti_1_l13") val betti1L13: Int? = null,
    @SerialName("topo_target_b0") val topoTargetB0: Int? = null,
    @SerialName("topo_target_b1") val topoTargetB1: Int? = null,
    @SerialName("topo_event") val topoEvent: Boolean? = null,
    @SerialName("topo_event_type") val topoEventType: String? = null,
    @SerialName("fused_embedding") val fusedEmbedding: List<Double>? = null,
    @SerialName("wolfram_steps") val wolframSteps: Int? = null,
    @SerialName("ml_cycles") val mlCycles: Int? = null,
    val stability: Double? = null,
    @SerialName("adjustments_mean") val adjustmentsMean: Double? = null,
    @SerialName("vsa_similarity") val vsaSimilarity: Double? = null,
    @SerialName("vsa_memory_size") val vsaMemorySize: Int? = null,
)
