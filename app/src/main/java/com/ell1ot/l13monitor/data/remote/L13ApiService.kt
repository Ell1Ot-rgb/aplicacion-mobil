package com.ell1ot.l13monitor.data.remote

import com.ell1ot.l13monitor.data.remote.dto.HealthResponse
import com.ell1ot.l13monitor.data.remote.dto.InferRequest
import com.ell1ot.l13monitor.data.remote.dto.InferResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface L13ApiService {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("infer")
    suspend fun infer(@Body body: InferRequest): InferResponse
}
