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
import com.example.l13brain.ui.theme.CalcBezel
import com.example.l13brain.ui.theme.CalcBevelBorder
import com.example.l13brain.ui.theme.CalcBorderSubtle
import com.example.l13brain.ui.theme.CalcKey2ndBg
import com.example.l13brain.ui.theme.CalcKeyAlphaBg
import com.example.l13brain.ui.theme.CalcKeyVarBg
import com.example.l13brain.ui.theme.CalcLcdBackground
import com.example.l13brain.ui.theme.CalcLcdText
import com.example.l13brain.ui.theme.CalcLcdTextMuted
import com.example.l13brain.ui.theme.CalcPlotY1
import com.example.l13brain.ui.theme.CalcPlotY2
import com.example.l13brain.ui.theme.CalcPlotY3
import com.example.l13brain.ui.theme.CalcSoftkeyActiveBg
import com.example.l13brain.ui.theme.CalcSoftkeyActiveBorder
import com.example.l13brain.ui.theme.CalcSoftkeyActiveText
import com.example.l13brain.ui.theme.CalcSoftkeyBg
import com.example.l13brain.ui.theme.CalcSoftkeyBorder

@Composable
fun TopHeader(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onOpenArtemis: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenCalculator: () -> Unit = {},
    statusMessage: String = "WSS: [CONNECTED 12ms]",
    tick: Long = 248L,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CalcBezel, RoundedCornerShape(8.dp))
            .border(1.2.dp, CalcBevelBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // 1. Top Bar: Calculator Model Title + Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with Graphing Calculator Indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (isPaused) CalcPlotY2 else CalcPlotY1, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TI-L13 GRAPHING // MATRIX v3.5",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CalcPlotY1,
                    maxLines = 1
                )
            }

            // Top Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tick indicator (LCD coordinate step counter)
                Box(
                    modifier = Modifier
                        .border(1.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                        .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "T:$tick",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalcPlotY2
                    )
                }

                // Artemis AI Assistant Badge
                Box(
                    modifier = Modifier
                        .border(1.dp, CalcPlotY1.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .background(CalcKeyVarBg, RoundedCornerShape(4.dp))
                        .clickable { onOpenArtemis() }
                        .padding(horizontal = 5.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🤖 ARTEMIS",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalcPlotY1
                    )
                }

                // Calculator Matrix Dialog Button
                Box(
                    modifier = Modifier
                        .border(1.dp, CalcPlotY2.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .background(CalcKey2ndBg, RoundedCornerShape(4.dp))
                        .clickable { onOpenCalculator() }
                        .padding(horizontal = 5.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🧮 CALC",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalcPlotY2
                    )
                }

                // Guide Badge Button
                Box(
                    modifier = Modifier
                        .border(1.dp, CalcPlotY3.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .background(CalcKeyAlphaBg, RoundedCornerShape(4.dp))
                        .clickable { onOpenGuide() }
                        .padding(horizontal = 5.dp, vertical = 2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📖 GUIDE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CalcPlotY3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Softkeys Row (F1: GRAFO, F2: REPL, F3: TELEMETRÍA, F4: AJUSTES)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderTabItem(
                fKey = "F1",
                title = "GRAFO",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                fKey = "F2",
                title = "REPL",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                fKey = "F3",
                title = "TELEM",
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                modifier = Modifier.weight(1f)
            )

            HeaderTabItem(
                fKey = "F4",
                title = "AJUSTES",
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // 3. Graphing Calculator LCD Annunciator Strip (RAD | NORMAL | FLOAT | REAL | FULL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalcLcdBackground, RoundedCornerShape(4.dp))
                .border(0.8.dp, CalcBorderSubtle, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RAD",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = CalcPlotY1
            )
            Text(
                text = "NORMAL",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = CalcLcdTextMuted
            )
            Text(
                text = "FLOAT 6",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = CalcPlotY2
            )
            Text(
                text = "REAL",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = CalcPlotY3
            )
            Text(
                text = if (isPaused) "PAUSED" else "RUNNING",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPaused) CalcPlotY2 else CalcPlotY3
            )
            Text(
                text = "🔋 100%",
                fontFamily = FontFamily.Monospace,
                fontSize = 8.sp,
                color = CalcLcdText
            )
        }
    }
}

@Composable
private fun HeaderTabItem(
    fKey: String,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF52525B)
    val backgroundColor = if (isSelected) Color(0xFFFFFFFF) else Color(0xFF18181B)
    val textColor = if (isSelected) Color(0xFF000000) else Color(0xFFFFFFFF)
    val fKeyColor = if (isSelected) Color(0xFF000000) else Color(0xFFA1A1AA)

    Box(
        modifier = modifier
            .height(32.dp)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(6.dp))
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = fKey,
                fontFamily = FontFamily.Monospace,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = fKeyColor,
                lineHeight = 9.sp
            )
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontSize = 8.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                lineHeight = 10.sp
            )
        }
    }
}
