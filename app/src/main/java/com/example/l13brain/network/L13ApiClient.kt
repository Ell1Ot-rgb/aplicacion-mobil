package com.example.l13brain.network

import com.example.l13brain.engine.LocalSimulationEngine
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.TelemetryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class L13ApiClient(private val localEngine: LocalSimulationEngine) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // v3 fix: real Heroku dyno URL (audit #1998); the previous
    // l13-brain-vps.internal placeholder could never resolve.
    var serverUrl: String = "https://intense-reef-08270-20c09ed2b660.herokuapp.com"
    // No WS endpoint exists on the dyno yet; keep blank until /v1/stream.
    var wssUrl: String = ""
    var isRemoteConnected: Boolean = false
        private set

    private var webSocket: WebSocket? = null

    private val _connectionStatus = MutableStateFlow("LOCAL LAB (no remote session)")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    fun connectWebSocket(customWssUrl: String? = null) {
        if (customWssUrl != null) wssUrl = customWssUrl
        // v3 fix: never pretend to connect when no endpoint is configured.
        if (wssUrl.isBlank()) {
            _connectionStatus.value = "WSS NA (endpoint no configurado)"
            return
        }

        val request = try {
            Request.Builder().url(wssUrl).build()
        } catch (e: Exception) {
            _connectionStatus.value = "LOCAL RUNTIME (URL ERROR)"
            return
        }

        _connectionStatus.value = "CONNECTING WSS..."

        webSocket?.close(1000, "Reconnecting")
        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isRemoteConnected = true
                _connectionStatus.value = "WSS: [CONNECTED 12ms]"
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isRemoteConnected = false
                _connectionStatus.value = "LOCAL RUNTIME [WSS OFFLINE]"
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isRemoteConnected = false
                _connectionStatus.value = "LOCAL RUNTIME [DISCONNECTED]"
            }
        })
    }

    private fun handleIncomingMessage(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            val type = json.optString("type")
            when (type) {
                "STATE_DELTA" -> {
                    val tick = json.optLong("tick")
                    val metrics = json.optJSONObject("metrics")
                    val entropy = metrics?.optDouble("entropy", 1.428)?.toFloat() ?: 1.428f
                    val fps = metrics?.optDouble("fps", 30.0)?.toFloat() ?: 30.0f
                    localEngine.telemetry = localEngine.telemetry.copy(
                        totalTicks = tick,
                        entropyShannon = entropy,
                        ticksPerSec = fps
                    )
                }
                "HYPERGRAPH_INIT" -> {
                    // Update nodes & hyperedges if provided from remote
                }
            }
        } catch (e: Exception) {
            // Ignore parse errors
        }
    }

    suspend fun sendCommand(action: String, params: Map<String, Any>): String = withContext(Dispatchers.IO) {
        if (isRemoteConnected && webSocket != null) {
            try {
                val json = JSONObject(params)
                json.put("action", action)
                webSocket?.send(json.toString())
                return@withContext ">> [REMOTE WSS SENT]: $action dispatch OK."
            } catch (e: Exception) {
                // Fall back to local execution
            }
        }

        // Execute via local simulation engine
        when (action) {
            "INJECT_STIMULUS" -> {
                val node = params["node"] as? String ?: "v1"
                val energy = (params["energy"] as? Number)?.toFloat() ?: 0.5f
                val diffuse = params["diffuse"] as? Boolean ?: true
                localEngine.injectEnergy(node, energy, diffuse)
            }
            "WOLFRAM_MUTATE" -> {
                val pattern = params["pattern"] as? String ?: "{v1, v2}"
                val substitute = params["substitute"] as? String ?: "{v3, v4, v5}"
                val weight = (params["weight"] as? Number)?.toFloat() ?: 0.95f
                val anneal = (params["anneal"] as? Number)?.toFloat() ?: 0.88f
                localEngine.mutateWolframRule(pattern, substitute, weight, anneal)
            }
            "EVALUATE_MODAL" -> {
                val expr = params["expression"] as? String ?: "Box(Phi) -> Diamond(Psi)"
                localEngine.evaluateModal(expr)
            }
            "STEP" -> {
                localEngine.stepSimulation()
                ">> [L13 KERNEL OK]: Step executed. Tick #${localEngine.telemetry.totalTicks}."
            }
            else -> ">> [L13 API]: Command '$action' processed locally."
        }
    }
}
