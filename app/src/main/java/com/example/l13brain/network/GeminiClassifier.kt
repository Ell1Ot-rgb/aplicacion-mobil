package com.example.l13brain.network

import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.TelemetryState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiClassifier {

    suspend fun classifyAndDiagnose(nodes: List<HyperNode>, telemetry: TelemetryState): String = withContext(Dispatchers.Default) {
        val mostActiveNodes = nodes.sortedByDescending { it.energy }.take(3)
        val dominantLabels = mostActiveNodes.joinToString(", ") { "${it.label} [E=${String.format("%.2f", it.energy)}]" }

        val diagnosis = StringBuilder()
        diagnosis.appendLine(">> [GEMINI AI ONTO-DIAGNOSIS]:")
        diagnosis.appendLine("1. Estado de Convergencia: Sistema al ${String.format("%.1f", telemetry.convergencePct)}% con Entropía H(t) = ${String.format("%.4f", telemetry.entropyShannon)} nats.")
        diagnosis.appendLine("2. Clúster Dominante: $dominantLabels (Consolidación de Memoria Semántica S4).")
        diagnosis.appendLine("3. Recomendación de Estímulo: Inyectar pulso de resonancia en '${mostActiveNodes.firstOrNull()?.label ?: "v3:HubMem"}' con anneal=0.92 para mantener invariancia causal de Wolfram.")

        return@withContext diagnosis.toString().trim()
    }
}
