package com.example.l13brain.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopHeader(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenArtemis: () -> Unit,
    onOpenGuide: () -> Unit,
    statusMessage: String = "WSS: [CONNECTED 12ms]",
    tick: Long = 248L,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF03070E))
            .border(1.2.dp, Color(0xFF103328), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // 1. Top Bar: App Title + Artemis MCP & Guide Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with Pulsing Phosphor Green Dot
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isPaused) Color(0xFFFFB300) else Color(0xFF00FF66), CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TOP-TUI PRO v3.5 // L13 BRAIN",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = Color(0xFF00FF66),
                    maxLines = 1
                )
            }

            // Top Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tick indicator
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF16382B), RoundedCornerShape(4.dp))
                        .background(Color(0xFF061017), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "TICK:$tick",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFB300)
                    )
                }

                // Artemis MCP Badge Button
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF00E5FF), RoundedCornerShape(4.dp))
                        .background(Color(0xFF0A1926), RoundedCornerShape(4.dp))
                        .clickable { onOpenArtemis() }
                        .padding(horizontal = 6.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤖 ARTEMIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E5FF)
                    )
                }

                // Guide Badge Button
                Box(
                    modifier = Modifier
                        .border(1.dp, Color(0xFF335C4A), RoundedCornerShape(4.dp))
                        .background(Color(0xFF0A1926), RoundedCornerShape(4.dp))
                        .clickable { onOpenGuide() }
                        .padding(horizontal = 5.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📖 GUIDE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF86EFAC)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Tab Navigation Bar (4 Tabs matching screenshots)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderTabItem(
                title = "🌐 GRAFO",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                title = "💻 REPL",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                title = "📊 TELEMETRÍA",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                title = "⚙️ AJUSTES",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // 3. Status Sub-strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF02050A), RoundedCornerShape(4.dp))
                .border(0.8.dp, Color(0xFF0C2418), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MODO: AMALGAMADA DPO",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF66)
            )
            Text(
                text = "FRACTAL: L13",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = Color(0xFF00E5FF)
            )
            Text(
                text = if (isPaused) "ESTADO: PAUSADO" else "ESTADO: AUTOPOIÉTICO",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = if (isPaused) Color(0xFFFFB300) else Color(0xFF86EFAC)
            )
            Text(
                text = "MEM: 14.2MB",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}

@Composable
private fun HeaderTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFF00FF66) else Color(0xFF143022)
    val backgroundColor = if (isSelected) Color(0xFF072618) else Color(0xFF070E17)
    val textColor = if (isSelected) Color(0xFF00FF66) else Color(0xFF4F7363)

    Box(
        modifier = modifier
            .height(32.dp)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(6.dp))
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
