package com.ell1ot.l13monitor.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.List
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ell1ot.l13monitor.ui.control.ControlScreen
import com.ell1ot.l13monitor.ui.dashboard.DashboardScreen
import com.ell1ot.l13monitor.ui.history.HistoryScreen
import com.ell1ot.l13monitor.ui.hypergraph.HypergraphScreen
import com.ell1ot.l13monitor.ui.settings.SettingsScreen

enum class Route(val path: String, val label: String) {
    DASHBOARD("dashboard", "Dashboard"),
    CONTROL("control", "Control"),
    HYPERGRAPH("hypergraph", "Graph"),
    HISTORY("history", "History"),
    SETTINGS("settings", "Settings"),
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val current = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                Route.entries.forEach { route ->
                    NavigationBarItem(
                        selected = current == route.path,
                        onClick = { navController.navigate(route.path) { launchSingleTop = true } },
                        icon = {
                            when (route) {
                                Route.DASHBOARD -> Icon(Icons.Filled.Home, contentDescription = route.label)
                                Route.CONTROL -> Icon(Icons.Filled.PlayArrow, contentDescription = route.label)
                                Route.HYPERGRAPH -> Icon(Icons.Outlined.Info, contentDescription = route.label)
                                Route.HISTORY -> Icon(Icons.Outlined.List, contentDescription = route.label)
                                Route.SETTINGS -> Icon(Icons.Filled.Settings, contentDescription = route.label)
                            }
                        },
                        label = { Text(route.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.DASHBOARD.path,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.DASHBOARD.path) { DashboardScreen() }
            composable(Route.CONTROL.path) { ControlScreen() }
            composable(Route.HYPERGRAPH.path) { HypergraphScreen() }
            composable(Route.HISTORY.path) { HistoryScreen() }
            composable(Route.SETTINGS.path) { SettingsScreen() }
        }
    }
}
