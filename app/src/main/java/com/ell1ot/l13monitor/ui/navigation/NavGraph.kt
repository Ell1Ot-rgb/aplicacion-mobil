package com.ell1ot.l13monitor.ui.navigation

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ell1ot.l13monitor.ui.control.ControlScreen
import com.ell1ot.l13monitor.ui.dashboard.DashboardScreen
import com.example.l13brain.ui.L13ViewModel
import com.example.l13brain.ui.TopTuiViewModel
import com.example.l13brain.ui.screens.TopTuiMainScreen

enum class Route(val path: String, val label: String, val channel: String) {
    TOPTUI("toptui", "TOPOSCOPIO", "CH1"),
    CONTROL("control", "CONTROL HW", "CH2"),
    DASHBOARD("dashboard", "MÉTRICAS", "CH3"),
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route ?: Route.TOPTUI.path

    val topTuiViewModel: TopTuiViewModel = viewModel()
    val l13ViewModel: L13ViewModel = viewModel()

    Scaffold(
        containerColor = Color(0xFF03070E),
        bottomBar = {
            // CNC Industrial Instrument Bottom Navigation Dock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF03070E))
                    .border(1.dp, Color(0xFF0E2218))
            ) {
                // Top scanning phosphor neon edge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF00FF66).copy(alpha = 0.40f))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Route.entries.forEach { route ->
                        val isSelected = current == route.path
                        val activeColor = when (route) {
                            Route.TOPTUI -> Color(0xFF00FF66)
                            Route.CONTROL -> Color(0xFF00E5FF)
                            Route.DASHBOARD -> Color(0xFFFFB300)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .background(
                                    if (isSelected) activeColor.copy(alpha = 0.14f) else Color(0xFF060B12),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) activeColor else Color(0xFF12222E),
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable {
                                    navController.navigate(route.path) {
                                        launchSingleTop = true
                                    }
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(
                                                if (isSelected) activeColor else Color(0xFF283A32),
                                                CircleShape
                                            )
                                    )

                                    Text(
                                        text = "[${route.channel}]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) activeColor.copy(alpha = 0.85f) else Color(0xFF476056),
                                        maxLines = 1
                                    )
                                }

                                Text(
                                    text = route.label,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) activeColor else Color(0xFF7A9388),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.TOPTUI.path,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.TOPTUI.path) {
                TopTuiMainScreen(
                    topTuiViewModel = topTuiViewModel,
                    l13ViewModel = l13ViewModel
                )
            }
            composable(Route.DASHBOARD.path) { DashboardScreen() }
            composable(Route.CONTROL.path) { ControlScreen() }
        }
    }
}

