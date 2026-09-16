package com.example.l13brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import com.example.l13brain.model.HyperEdge
import com.example.l13brain.model.HyperNode

@Composable
fun HypergraphInspectorDialog(
    selectedNode: HyperNode?,
    allNodes: List<HyperNode>,
    allEdges: List<HyperEdge>,
    onDismiss: () -> Unit,
    onInjectEnergy: (Float) -> Unit,
    onResetPhase: () -> Unit,
    onDeleteNode: (String) -> Unit,
    onAddNode: (String, Float, String) -> Unit,
    onAddHyperEdge: (String, List<String>, Float) -> Unit
) {
    var showAddNodeForm by remember { mutableStateOf(false) }
    var showAddEdgeForm by remember { mutableStateOf(false) }

    var newNodeLabel by remember { mutableStateOf("") }
    var newNodeEnergy by remember { mutableStateOf("1.0") }
    var newNodeModal by remember { mutableStateOf("Sensorial") }

    var newEdgeLabel by remember { mutableStateOf("") }
    var newEdgeNodes by remember { mutableStateOf("") }
    var newEdgeWeight by remember { mutableStateOf("1.2") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF0B131C),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF1E4032), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "INSPECTOR Y EDITOR DE TOPOLOGÍA L13",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF00FF66)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedNode != null && !showAddNodeForm && !showAddEdgeForm) {
                    // Selected Node Detail View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF060B12), RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFF16382B), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Text("ID: ${selectedNode.id} | LABEL: ${selectedNode.label}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00E5FF))
                        Text("ENERGÍA E(v): ${String.format("%.3f", selectedNode.energy)} J", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        Text("FASE DE KURAMOTO θ(v): ${String.format("%.3f", selectedNode.phase)} rad (${(selectedNode.phase * 180 / Math.PI).toInt()}°)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                        Text("ESTADO MODAL S4: ${selectedNode.modalState}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFFFFB300))
                        Text("GRADO d(v): ${selectedNode.degree} hiperaristas", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF94A3B8))
                        Text("COORDENADAS (x, y): (${String.format("%.2f", selectedNode.x)}, ${String.format("%.2f", selectedNode.y)})", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color(0xFF94A3B8))

                        val incidentEdges = allEdges.filter { selectedNode.id in it.nodeIds }.map { it.label }
                        Text("INCIDENTE EN: ${incidentEdges.joinToString(", ")}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFFFF4081))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ElevatedButton(
                            onClick = { onInjectEnergy(0.50f) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF0A3A28), contentColor = Color(0xFF00FF66))
                        ) {
                            Text("+0.5J Estímulo", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }

                        ElevatedButton(
                            onClick = onResetPhase,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF0D3344), contentColor = Color(0xFF00E5FF))
                        ) {
                            Text("Reset Fase θ=0", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedButton(
                        onClick = { onDeleteNode(selectedNode.id) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4081)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5C1D33))
                    ) {
                        Text("Eliminar Nodo (${selectedNode.label})", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }

                if (showAddNodeForm) {
                    Text("AÑADIR NUEVO NODO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00E5FF))
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newNodeLabel,
                        onValueChange = { newNodeLabel = it },
                        label = { Text("Etiqueta (ej. s_tactual)", fontSize = 10.sp) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newNodeEnergy,
                        onValueChange = { newNodeEnergy = it },
                        label = { Text("Energía Inicial (J)", fontSize = 10.sp) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newNodeModal,
                        onValueChange = { newNodeModal = it },
                        label = { Text("Estado Modal (ej. [] Phi)", fontSize = 10.sp) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(
                            onClick = {
                                if (newNodeLabel.isNotBlank()) {
                                    val e = newNodeEnergy.toFloatOrNull() ?: 1.0f
                                    onAddNode(newNodeLabel, e, newNodeModal)
                                    showAddNodeForm = false
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF0A3A28), contentColor = Color(0xFF00FF66)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar Nodo", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showAddNodeForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }

                if (showAddEdgeForm) {
                    Text("AÑADIR HIPERARISTA N-ARIA", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFFFB300))
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = newEdgeLabel,
                        onValueChange = { newEdgeLabel = it },
                        label = { Text("Nombre (ej. e_triada_sensorial)", fontSize = 10.sp) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newEdgeNodes,
                        onValueChange = { newEdgeNodes = it },
                        label = { Text("IDs de Nodos separados por coma", fontSize = 10.sp) },
                        placeholder = { Text(allNodes.take(3).map { it.id }.joinToString(", "), fontSize = 10.sp, color = Color.Gray) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newEdgeWeight,
                        onValueChange = { newEdgeWeight = it },
                        label = { Text("Peso / Acoplamiento (W)", fontSize = 10.sp) },
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ElevatedButton(
                            onClick = {
                                val nodeIds = newEdgeNodes.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                if (newEdgeLabel.isNotBlank() && nodeIds.size >= 2) {
                                    val w = newEdgeWeight.toFloatOrNull() ?: 1.0f
                                    onAddHyperEdge(newEdgeLabel, nodeIds, w)
                                    showAddEdgeForm = false
                                }
                            },
                            colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF38260D), contentColor = Color(0xFFFFB300)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Guardar Hiperarista", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showAddEdgeForm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!showAddNodeForm && !showAddEdgeForm) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showAddNodeForm = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Añadir Nodo", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }

                        OutlinedButton(
                            onClick = { showAddEdgeForm = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+ Hiperarista", fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ElevatedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.elevatedButtonColors(containerColor = Color(0xFF1A2230), contentColor = Color.White)
                ) {
                    Text("Cerrar Inspector", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }
}
