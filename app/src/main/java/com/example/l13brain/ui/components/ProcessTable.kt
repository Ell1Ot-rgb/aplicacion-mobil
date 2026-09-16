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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.l13brain.model.ProcessInfo

@Composable
fun ProcessTable(
    processes: List<ProcessInfo>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF070B12))
            .border(1.dp, Color(0xFF143022), RoundedCornerShape(6.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▶ PROCESOS HOST & WORKERS DEL KERNEL",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "${processes.size} ACTIVOS",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F1A1F), RoundedCornerShape(2.dp))
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("PID", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(1.0f))
            Text("USER", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(1.2f))
            Text("CPU%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(1.2f))
            Text("MEM%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(1.2f))
            Text("STAT", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(1.0f))
            Text("COMMAND", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF00E5FF), modifier = Modifier.weight(2.8f))
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Process Rows
        processes.take(5).forEach { proc ->
            val textColor = if (proc.isEngineProcess) Color(0xFF00FF66) else Color(0xFFCBD5E1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(proc.pid.toString(), fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor, modifier = Modifier.weight(1.0f))
                Text(proc.user, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor, modifier = Modifier.weight(1.2f))
                Text("${proc.cpuPct}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = if (proc.cpuPct > 20f) Color(0xFFFFB300) else textColor, modifier = Modifier.weight(1.2f))
                Text("${proc.memPct}%", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor, modifier = Modifier.weight(1.2f))
                Text(proc.status, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor, modifier = Modifier.weight(1.0f))
                Text(proc.command, fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = textColor, modifier = Modifier.weight(2.8f), maxLines = 1)
            }
        }

    }
}
