package com.example.l13brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode
import com.example.l13brain.model.TelemetryState
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * HYPERGRAPH SPECTRAL CALCULATOR — v3 honest rewrite (audit #2023 / #2024).
 *
 * Before v3 this dialog printed hard-coded eigenvalues/traces that contradicted
 * the very formula it displayed. All numbers shown here are now COMPUTED from
 * the graph passed in (Zhou normalized hypergraph Laplacian) or shown as "NA".
 *
 * Math (positive weights, positive degrees):
 *   L = I - Dv^(-1/2) H W De^(-1) Hᵀ Dv^(-1/2)
 *  => spec(L) ⊆ [0, 1]; multiplicity of λ=0 equals #weakly-connected components.
 */
@Composable
fun HypergraphSpectralCalculatorDialog(
    telemetry: TelemetryState,
    nodes: List<HyperNode> = emptyList(),
    hyperedges: List<HyperEdge> = emptyList(),
    onDismiss: () -> Unit,
    onExecuteCommand: (String) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .background(Color(0xFF03070E), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF00FF66), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxHeight()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "CALCULADORA ESPECTRAL L13 (valores COMPUTADOS o NA)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color(0xFF00FF66)
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(Color(0xFF0A1F16), CircleShape)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✕", color = Color(0xFF00FF66), fontSize = 11.sp)
                    }
                }
                Text(
                    "L = I − Dv^(-1/2)·H·W·De^(-1)·Hᵀ·Dv^(-1/2) | spec(L) ⊆ [0,1]",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ESPECTRO REAL", "REPL ARITMÉTICO").forEachIndexed { i, t ->
                        val sel = activeTab == i
                        Box(
                            modifier = Modifier
                                .background(
                                    if (sel) Color(0xFF00FF66).copy(alpha = 0.18f) else Color(0xFF060F16),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(1.dp, if (sel) Color(0xFF00FF66) else Color(0xFF133224), RoundedCornerShape(4.dp))
                                .clickable { activeTab = i }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(t, fontFamily = FontFamily.Monospace, fontSize = 9.sp,
                                color = if (sel) Color(0xFF00FF66) else Color(0xFF7A9388))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                when (activeTab) {
                    0 -> RealSpectrumTab(nodes, hyperedges)
                    else -> ArithmeticReplTab(onExecuteCommand)
                }
            }
        }
    }
}

/** Spectrum of the Zhou normalized hypergraph Laplacian, computed from live state. */
private fun computeSpectrum(nodes: List<HyperNode>, hyperedges: List<HyperEdge>): FloatArray {
    val n = nodes.size
    if (n == 0) return FloatArray(0)
    val nodeIndex = HashMap<String, Int>(n)
    nodes.forEachIndexed { i, nd -> nodeIndex[nd.id] = i }

    val dV = FloatArray(n)
    for (e in hyperedges) {
        for (v in e.nodeIds) {
            nodeIndex[v]?.let { dV[it] += e.weight }
        }
    }
    val L = Array(n) { i -> FloatArray(n) { j -> if (i == j) 1f else 0f } }
    for (e in hyperedges) {
        val de = e.nodeIds.size.coerceAtLeast(1).toFloat()
        val factor = e.weight / de
        val members = e.nodeIds.mapNotNull { nodeIndex[it] }
        for (u in members) {
            val degU = if (dV[u] > 1e-6f) sqrt(dV[u]) else 1f
            for (v in members) {
                val degV = if (dV[v] > 1e-6f) sqrt(dV[v]) else 1f
                L[u][v] -= factor / (degU * degV)
            }
        }
    }
    return jacobiEigenvalues(L)
}

/** Classic symmetric Jacobi with correct mirror updates (audit fix). */
internal fun jacobiEigenvalues(a0: Array<FloatArray>): FloatArray {
    val n = a0.size
    if (n == 0) return FloatArray(0)
    if (n == 1) return floatArrayOf(a0[0][0])
    val m = Array(n) { i -> a0[i].copyOf() }
    repeat(50) {
        var maxVal = 0f; var p = 0; var q = 1
        for (i in 0 until n) for (j in i + 1 until n) {
            val av = abs(m[i][j]); if (av > maxVal) { maxVal = av; p = i; q = j }
        }
        if (maxVal < 1e-6f) return FloatArray(n) { m[it][it] }.sortedArrayStyle()
        val app = m[p][p]; val aqq = m[q][q]; val apq = m[p][q]
        val diff = aqq - app
        val theta = if (abs(diff) < 1e-9f) (PI / 4.0).toFloat()
        else 0.5f * atan(2f * apq / diff)
        val c = cos(theta); val s = sin(theta)
        for (k in 0 until n) {
            if (k != p && k != q) {
                val akp = m[k][p]; val akq = m[k][q]
                m[k][p] = c * akp - s * akq; m[p][k] = m[k][p]
                m[k][q] = s * akp + c * akq; m[q][k] = m[k][q]
            }
        }
        m[p][p] = c * c * app - 2f * s * c * apq + s * s * aqq
        m[q][q] = s * s * app + 2f * s * c * apq + c * c * aqq
        m[p][q] = 0f; m[q][p] = 0f
    }
    return FloatArray(n) { m[it][it] }.sortedArrayStyle()
}

private fun FloatArray.sortedArrayStyle(): FloatArray { sort(); return this }

@Composable
private fun RealSpectrumTab(nodes: List<HyperNode>, hyperedges: List<HyperEdge>) {
    val spectrum = remember(nodes, hyperedges) {
        if (nodes.isEmpty()) floatArrayOf() else computeSpectrum(nodes, hyperedges)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (spectrum.isEmpty()) {
            Text(
                "NA — no hay grafo disponible para computar el espectro.\n" +
                    "Sin nodos/aristas no se afirma ningún valor (antes se mostraban literales).",
                fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color(0xFFFFB300)
            )
            return@Column
        }
        val trace = spectrum.sum()
        val fiedler = spectrum.getOrElse(1) { 0f }
        val zeroMult = spectrum.count { it < 1e-4f }
        InfoRow("n = ${spectrum.size} nodos | m = ${hyperedges.size} hiperaristas")
        InfoRow("λ = [" + spectrum.joinToString(", ") { "%.4f".format(it) } + "]")
        InfoRow("Tr(L) = %.4f (computado, suma de autovalores)".format(trace))
        InfoRow("λ₁ (Fiedler) = %.4f".format(fiedler))
        InfoRow("multiplicidad(λ=0) = $zeroMult (≈ componentes débilmente conexas)")
        if (spectrum.any { it > 1.0001f }) {
            Text(
                "ADVERTENCIA: autovalor > 1 para Laplaciano normalizado Zhou,\n" +
                    "indica entrada inválida (peso negativo / grado nulo).",
                fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFFF5252)
            )
        }
        NoteBox(
            "λ min = 0 NO basta para afirmar conexidad: se requiere multiplicidad\n" +
                "exactamente 1. Este panel ya no imprime valores literales."
        )
    }
}

@Composable
private fun InfoRow(text: String) {
    Text(text, fontFamily = FontFamily.Monospace, fontSize = 8.5.sp, color = Color(0xFFE2E8F0))
}

@Composable
private fun NoteBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF160814), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFFFF4081), RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Text(text, fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFFFFB3C9))
    }
}

/** Simple arithmetic REPL: supports + - * / ( ) and explicit results/errors. */
@Composable
private fun ArithmeticReplTab(onExecuteCommand: (String) -> Unit) {
    var input by remember { mutableStateOf("2+2") }
    var output by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Solo aritmética (+ - * / ^ paréntesis). No se muestran matrices ni\n" +
                "espectros hardcodeados; los históricos literal se eliminaron (v3).",
            fontFamily = FontFamily.Monospace, fontSize = 8.sp, color = Color(0xFF94A3B8)
        )
        TextField(
            value = input,
            onValueChange = { input = it },
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF00FF66)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF02060C),
                unfocusedContainerColor = Color(0xFF02060C)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF00FF66).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                .border(0.8.dp, Color(0xFF00FF66), RoundedCornerShape(4.dp))
                .clickable {
                    val text = input // v3 fix: evaluate the CURRENT text, not the stale previous one
                    output = try {
                        "= ${EvalArith.eval(text)}"
                    } catch (e: Exception) {
                        "ERROR: ${e.message}"
                    }
                    onExecuteCommand("calc $text")
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Evaluar ▶", fontFamily = FontFamily.Monospace, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
        }
        if (output.isNotEmpty()) {
            Text(output, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.White)
        }
    }
}

/** Minimal recursive-descent arithmetic parser: no science-notation corruption, no silent 0.0. */
object EvalArith {
    fun eval(src: String): Double {
        val s = src.trim()
        require(s.isNotEmpty()) { "expresión vacía" }
        val it = Parser(s)
        val v = it.parseExpr()
        it.skipWs()
        if (!it.eof()) throw IllegalArgumentException("token inesperado en posición ${it.pos}")
        if (v.isNaN()) throw ArithmeticException("resultado NaN (p.ej. 0/0)")
        if (v.isInfinite()) throw ArithmeticException("resultado infinito (división por cero)")
        return v
    }

    private class Parser(val s: String, var pos: Int = 0) {
        fun eof() = pos >= s.length
        fun skipWs() { while (!eof() && s[pos] == ' ') pos++ }
        private fun peek(): Char = if (eof()) '' else s[pos]

        fun parseExpr(): Double {
            var v = parseTerm()
            while (true) {
                skipWs()
                when { tryTake('+') -> v += parseTerm(); tryTake('-') -> v -= parseTerm(); else -> return v }
            }
        }

        private fun parseTerm(): Double {
            var v = parseFactor()
            while (true) {
                skipWs()
                when { tryTake('*') -> v *= parseFactor(); tryTake('/') -> v /= parseFactor(); else -> return v }
            }
        }

        private fun parseFactor(): Double {
            skipWs()
            if (tryTake('-')) return -parseFactor()
            if (tryTake('+')) return parseFactor()
            val b = parsePrimary()
            skipWs()
            return if (tryTake('^')) Math.pow(b, parseFactor()) else b
        }

        private fun parsePrimary(): Double {
            skipWs()
            if (tryTake('(')) {
                val v = parseExpr(); skipWs()
                if (!tryTake(')')) throw IllegalArgumentException("falta ')'")
                return v
            }
            val start = pos
            if (peek() == '-' || peek() == '+') pos++
            while (!eof() && (s[pos].isDigit() || s[pos] == '.' || s[pos] == 'e' || s[pos] == 'E' ||
                    ((s[pos] == '-' || s[pos] == '+') && pos > start &&
                        (s[pos - 1] == 'e' || s[pos - 1] == 'E')))) pos++
            if (pos == start) throw IllegalArgumentException("número esperado en posición $pos")
            return s.substring(start, pos).toDoubleOrNull()
                ?: throw IllegalArgumentException("número inválido en posición $start")
        }

        private fun tryTake(ch: Char): Boolean { skipWs(); return if (!eof() && s[pos] == ch) { pos++; true } else false }
    }
}
