package com.ell1ot.l13monitor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ell1ot.l13monitor.data.repository.CredentialsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val credentials: CredentialsRepository,
) : ViewModel() {
    val initialBase = credentials.baseUrl
    fun save(base: String, token: String) = credentials.save(base, token)
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var base by rememberSaveable { mutableStateOf(viewModel.initialBase) }
    var token by rememberSaveable { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Configuración", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = base,
            onValueChange = { base = it },
            label = { Text("L13 base URL") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Bearer token (no se muestra)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(onClick = { viewModel.save(base, token) }) { Text("Guardar") }
        Text("Se guarda en EncryptedSharedPreferences; pinning a herokuapp.com solo en release.")
    }
}
