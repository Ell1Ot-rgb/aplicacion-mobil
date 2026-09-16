package com.ell1ot.l13monitor.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(val status: String? = null)
