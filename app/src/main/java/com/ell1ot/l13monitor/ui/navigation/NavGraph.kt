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
        containerColor = Color(0xFF000000),
        bottomBar = {
            // Graphing Calculator Hardware Bottom Navigation Dock (High-Contrast B&W)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000000))
                    .border(1.dp, Color(0xFF27272A))
            ) {
                // Top sharp LCD separator line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFFFFFFF))
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Route.entries.forEach { route ->
                        val isSelected = current == route.path

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(
                                    if (isSelected) Color(0xFFFFFFFF) else Color(0xFF18181B),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    1.2.dp,
                                    if (isSelected) Color(0xFFFFFFFF) else Color(0xFF52525B),
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
                                                if (isSelected) Color(0xFF000000) else Color(0xFF71717A),
                                                CircleShape
                                            )
                                    )

                                    Text(
                                        text = "[${route.channel}]",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 7.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF000000) else Color(0xFFA1A1AA),
                                        maxLines = 1
                                    )
                                }

                                Text(
                                    text = route.label,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF000000) else Color(0xFFFFFFFF),
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

