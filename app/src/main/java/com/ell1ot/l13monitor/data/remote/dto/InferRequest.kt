package com.ell1ot.l13monitor.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Request body for POST /infer on the L13 server. */
@Serializable
data class InferRequest(
    val command: String? = null,
    val ticks: Int? = null,
    @SerialName("betti_0") val betti0: Int? = null,
    @SerialName("betti_1") val betti1: Int? = null,
    val intensity: Float? = null,
    val tau: Float? = null,
    @SerialName("thought_vector") val thoughtVector: List<Float>? = null,
    @SerialName("concept_vector") val conceptVector: List<Float>? = null,
    val confirm: Boolean? = null,
)
