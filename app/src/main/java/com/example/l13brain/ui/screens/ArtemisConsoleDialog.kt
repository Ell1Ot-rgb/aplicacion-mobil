package com.example.l13brain.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

@Composable
fun ArtemisConsoleDialog(
    onDismiss: () -> Unit,
    onExecuteAction: (String) -> Unit
) {
    var promptText by remember {
        mutableStateOf("Inyecta 0.5J al nodo s_optico y verifica sincronización Kuramoto...")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .background(Color(0xFF03070E), RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF00E5FF), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🤖", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "GOOGLE ARTEMIS // ON-DEVICE AUTOMATION",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF)
                            )
                        }
                        Text(
                            text = "Control autónomo de la UI en lenguaje natural sobre dispositivo Android",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // 2. Status Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF041810), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF00FF66), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(Color(0xFF00FF66), CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STATUS: ARTEMIS MCP AGENT CONECTADO AL TELÉFONO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color(0xFF00FF66)
                        )
                    }
                    Text(
                        text = "• Dispositivo objetivo: Streaming Virtual Android Device (Target API 35)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "• Perfil de ejecución: Flash Profile (99.2% benchmark pass)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }

                // 3. Prompt Input
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "INSTRUCCIÓN EN LENGUAJE NATURAL PARA EL DISPOSITIVO:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        color = Color(0xFF00FF66)
                    )

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.5.sp,
                            color = Color(0xFF00FF66)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF00E5FF),
                            unfocusedBorderColor = Color(0xFF103328),
                            focusedContainerColor = Color(0xFF03070E),
                            unfocusedContainerColor = Color(0xFF03070E)
                        )
                    )
                }

                // 4. Send Action Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00E5FF), RoundedCornerShape(6.dp))
                        .clickable { onExecuteAction(promptText) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "▶ Enviar Instrucción Artemis",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }

                // 5. Presets Row (Horizontally scrollable, full badges, no truncation)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "PRESETS RÁPIDOS DE CONTROL ARTEMIS:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val presets = listOf(
                            Pair("⚡ Inyectar +0.5J", "Inyecta 0.5J al nodo hub_central"),
                            Pair("🔄 Reescritura DPO", "Aplica reescritura DPO con regla Wolfram"),
                            Pair("🩺 Diagnóstico AI", "Ejecuta diagnóstico topológico completo"),
                            Pair("💾 Guardar Snapshot", "Guarda snapshot del hipergrafo a Firestore")
                        )
                        presets.forEach { (label, action) ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, Color(0xFF16382B), RoundedCornerShape(4.dp))
                                    .background(Color(0xFF060D15), RoundedCornerShape(4.dp))
                                    .clickable {
                                        promptText = action
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF86EFAC),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 6. Action Logs Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF040810), RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFF103328), RoundedCornerShape(6.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "REGISTRO DE ACCIONES Y TELÉFONO (2):",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp,
                        color = Color(0xFF00FF66)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[DEVICE_HANDSHAKE] -> #root_scaffold",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF072618), RoundedCornerShape(3.dp))
                                .border(0.8.dp, Color(0xFF00FF66), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("SUCCESS", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00FF66))
                        }
                    }
                    Text("Artemis MCP Driver enlazado con dispositivo Android", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF94A3B8))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ACCESSIBILITY_SCAN] -> #top_tui_main_screen",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF051C24), RoundedCornerShape(3.dp))
                                .border(0.8.dp, Color(0xFF00E5FF), RoundedCornerShape(3.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("ACTIVE", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E5FF))
                        }
                    }
                    Text("Escaneo semántico de nodos UI y testTags completado", fontFamily = FontFamily.Monospace, fontSize = 7.5.sp, color = Color(0xFF94A3B8))
                }

                // 7. Close Console Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00FF66), RoundedCornerShape(6.dp))
                        .clickable { onDismiss() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cerrar Consola",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
