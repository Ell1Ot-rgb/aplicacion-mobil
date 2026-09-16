package com.ell1ot.l13monitor.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Row as persisted server-side in l13_cycles (used when the server exposes a cycles endpoint). */
@Serializable
data class CycleDto(
    val cycle: Int? = null,
    val status: String? = null,
    val stability: Double? = null,
    @SerialName("topo_loss") val topoLoss: Double? = null,
    val ts: String? = null,
)
