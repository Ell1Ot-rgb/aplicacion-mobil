package com.ell1ot.l13monitor.ui.control

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ell1ot.l13monitor.core.commands.L13Command
import com.ell1ot.l13monitor.data.repository.L13Repository
import com.ell1ot.l13monitor.ui.components.CommandButton
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ControlState(
    val thoughtIntensity: Float = 0.5f,
    val tau: Float = 0.35f,
    val ackLog: String = "",
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
        // observe acks into log text
        viewModelScope.launch {
            _effects.tryEmit("bus listo")
        }
    }

    fun reduce(event: ControlEvent) {
        when (event) {
            ControlEvent.TriggerCycle -> {
                repo.sendCommand(L13Command.TriggerCycle())
                _effects.tryEmit("cycle enviado")
            }
            is ControlEvent.SetThought -> _state.value = _state.value.copy(thoughtIntensity = event.v)
            is ControlEvent.SetTau -> _state.value = _state.value.copy(tau = event.v)
            ControlEvent.Calibrate -> {
                repo.sendCommand(L13Command.CalibrateTau(tau = _state.value.tau))
                _effects.tryEmit("calibrate tau=${_state.value.tau}")
            }
            ControlEvent.Reset -> {
                repo.sendCommand(L13Command.ResetGraph(confirm = true))
                _effects.tryEmit("reset solicitado")
            }
            is ControlEvent.Ingest -> {
                repo.sendCommand(L13Command.IngestBetti(betti0 = event.b0, betti1 = event.b1, intensity = _state.value.thoughtIntensity))
                _effects.tryEmit("ingest b0=${event.b0} b1=${event.b1}")
            }
        }
    }
}

@Composable
fun ControlScreen(viewModel: ControlViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // surface effects as snackbars
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.effects.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Control del hipergrafo", style = MaterialTheme.typography.titleLarge)

        Text("Intensidad thought_vector: %.2f".format(state.thoughtIntensity))
        Slider(
            value = state.thoughtIntensity,
            onValueChange = { viewModel.reduce(ControlEvent.SetThought(it)) },
        )

        Text("Tau: %.2f".format(state.tau))
        Slider(value = state.tau, onValueChange = { viewModel.reduce(ControlEvent.SetTau(it)) })

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CommandButton(text = "Trigger", onClick = { viewModel.reduce(ControlEvent.TriggerCycle) })
            CommandButton(text = "Calibrate τ", onClick = { viewModel.reduce(ControlEvent.Calibrate) })
            CommandButton(text = "Reset", onClick = { viewModel.reduce(ControlEvent.Reset) })
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CommandButton(text = "Ingest b0=1 b1=0", onClick = { viewModel.reduce(ControlEvent.Ingest(1, 0)) })
            CommandButton(text = "Ingest b0=1 b1=1", onClick = { viewModel.reduce(ControlEvent.Ingest(1, 1)) })
        }

        Text("Últimos comandos", style = MaterialTheme.typography.titleLarge)
        LazyColumn {
            items(listOf("espera acción…")) { Text(it) }
        }
    }
}
