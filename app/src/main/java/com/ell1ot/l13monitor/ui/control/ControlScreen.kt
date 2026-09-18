package com.ell1ot.l13monitor.ui.control

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.data.local.db.CommandLogEntity
import com.ell1ot.l13monitor.data.repository.L13Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ControlState(
    val thoughtIntensity: Float = 0.5f,
    val tau: Float = 0.35f,
    val ackLog: String = "",
    val commandLogs: List<CommandLogEntity> = emptyList(),
)

sealed interface ControlEvent {
    data object TriggerCycle : ControlEvent
    data class SetThought(val v: Float) : ControlEvent
    data class SetTau(val v: Float) : ControlEvent
    data object Calibrate : ControlEvent
    data object Reset : ControlEvent
    data class Ingest(val b0: Int, val b1: Int) : ControlEvent
}

@HiltViewModel
class ControlViewModel @Inject constructor(
    private val repo: L13Repository,
) : ViewModel() {

    private val _state = MutableStateFlow(ControlState())
    val state: StateFlow<ControlState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val effects: SharedFlow<String> = _effects

    init {
        viewModelScope.launch {
            _effects.tryEmit("COMMAND BUS READY")
        }
        viewModelScope.launch {
            repo.commandHistory().collect { logs ->
                _state.update { it.copy(commandLogs = logs) }
            }
        }
    }

    fun reduce(event: ControlEvent) {
        when (event) {
            ControlEvent.TriggerCycle -> {
                repo.sendCommand(L13Command.TriggerCycle())
                _effects.tryEmit("⚡ TRIGGER CYCLE despachado al bus")
            }
            is ControlEvent.SetThought -> _state.update { it.copy(thoughtIntensity = event.v) }
            is ControlEvent.SetTau -> _state.update { it.copy(tau = event.v) }
            ControlEvent.Calibrate -> {
                repo.sendCommand(L13Command.CalibrateTau(tau = _state.value.tau))
                _effects.tryEmit("🎯 CALIBRATE τ=${String.format(Locale.US, "%.2f", _state.value.tau)}")
            }
            ControlEvent.Reset -> {
                repo.sendCommand(L13Command.ResetGraph(confirm = true))
                _effects.tryEmit("⚠️ RESET DEL GRAFO solicitado")
            }
            is ControlEvent.Ingest -> {
                repo.sendCommand(
                    L13Command.IngestBetti(
                        betti0 = event.b0,
                        betti1 = event.b1,
                        intensity = _state.value.thoughtIntensity,
                    )
                )
                _effects.tryEmit("📥 INGEST β₀=${event.b0}, β₁=${event.b1}")
            }
        }
    }
}

@Composable
fun ControlScreen(viewModel: ControlViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF000000)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "┌─ CONTROL DE HARDWARE & DISPATCHER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color(0xFFFFFFFF)
                )
                Box(
                    modifier = Modifier
                        .background(Color(0xFF18181B), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFF52525B), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "BUS: FIFO ACTIVO",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }

            // Slider 1: Thought Vector Intensity
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "INTENSIDAD THOUGHT VECTOR",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                        Text(
                            "${String.format(Locale.US, "%.2f", state.thoughtIntensity)} J",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                    Slider(
                        value = state.thoughtIntensity,
                        onValueChange = { viewModel.reduce(ControlEvent.SetThought(it)) },
                        valueRange = 0.05f..1.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFFFFF),
                            activeTrackColor = Color(0xFFFFFFFF),
                            inactiveTrackColor = Color(0xFF3F3F46)
                        )
                    )
                }
            }

            // Slider 2: Tau Decay Constant
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "CONSTANTE DE DECAIMIENTO TAU (τ)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFFFFF)
                        )
                        Text(
                            "${String.format(Locale.US, "%.2f", state.tau)} s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA1A1AA)
                        )
                    }
                    Slider(
                        value = state.tau,
                        onValueChange = { viewModel.reduce(ControlEvent.SetTau(it)) },
                        valueRange = 0.05f..2.5f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFFFFF),
                            activeTrackColor = Color(0xFFFFFFFF),
                            inactiveTrackColor = Color(0xFF3F3F46)
                        )
                    )
                }
            }

            // Action Buttons Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ControlActionBtn(
                    text = "[EXE] TRIGGER",
                    borderColor = Color(0xFFFFFFFF),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.reduce(ControlEvent.TriggerCycle) }
                )
                ControlActionBtn(
                    text = "[CALC] τ",
                    borderColor = Color(0xFFE4E4E7),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.reduce(ControlEvent.Calibrate) }
                )
                ControlActionBtn(
                    text = "[CLEAR] RST",
                    borderColor = Color(0xFFA1A1AA),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.reduce(ControlEvent.Reset) }
                )
            }

            // Action Buttons Row 2: Ingest Betti Pairs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ControlActionBtn(
                    text = "INGEST β₀=1, β₁=0",
                    borderColor = Color(0xFFD4D4D8),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.reduce(ControlEvent.Ingest(1, 0)) }
                )
                ControlActionBtn(
                    text = "INGEST β₀=1, β₁=1",
                    borderColor = Color(0xFFD4D4D8),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.reduce(ControlEvent.Ingest(1, 1)) }
                )
            }

            // Live Command Bus Log Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TELEMETRÍA DEL COMMAND BUS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    text = "${state.commandLogs.size} EN COLA/HISTORIAL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 8.5.sp,
                    color = Color(0xFFA1A1AA)
                )
            }

            // Command Bus Log List
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(6.dp))
            ) {
                if (state.commandLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Esperando comandos de despacho...\nPresione Trigger, Calibrate o Ingest para enviar.",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color(0xFFA1A1AA),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.commandLogs) { log ->
                            CommandLogRow(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommandLogRow(log: CommandLogEntity) {
    val statusColor = Color(0xFFFFFFFF)

    val timeStr = remember(log.createdAtMillis) {
        try {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            sdf.format(Date(log.createdAtMillis))
        } catch (_: Exception) {
            "${log.createdAtMillis}"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF141414), RoundedCornerShape(4.dp))
            .border(0.8.dp, Color(0xFF3F3F46), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(statusColor, CircleShape)
            )
            Text(
                text = "[$timeStr]",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                color = Color(0xFFA1A1AA)
            )
            Text(
                text = log.kind,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF)
            )
            Text(
                text = log.payloadJson.take(24),
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = Color(0xFFA1A1AA),
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .background(Color(0xFFFFFFFF), RoundedCornerShape(2.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = "${log.status} #${log.attempt}",
                fontFamily = FontFamily.Monospace,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF000000)
            )
        }
    }
}

@Composable
private fun ControlActionBtn(
    text: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(38.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF18181B),
            contentColor = Color(0xFFFFFFFF)
        ),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, borderColor, RoundedCornerShape(3.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF)
            )
        }
    }
}
