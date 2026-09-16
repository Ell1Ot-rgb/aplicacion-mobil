package com.ell1ot.l13monitor.data.remote

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path

@Serializable
data class DynoFormationDto(val type: String? = null, val quantity: Int? = 0, val size: String? = null)

@Serializable
data class ReleaseDto(val id: String? = null, val version: Int? = null, @kotlinx.serialization.SerialName("created_at") val createdAt: String? = null)

interface HerokuApiService {
    @GET("apps/{app}/formation")
    suspend fun formation(@Path("app") app: String): List<DynoFormationDto>

    @GET("apps/{app}/releases")
    suspend fun releases(@Path("app") app: String): List<ReleaseDto>
}
