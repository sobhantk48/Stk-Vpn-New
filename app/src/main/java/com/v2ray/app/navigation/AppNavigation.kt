package com.v2ray.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.v2ray.app.ui.dashboard.DashboardScreen
import com.v2ray.app.ui.location.LocationListScreen
import com.v2ray.app.ui.settings.LogViewerScreen
import com.v2ray.app.ui.settings.SettingsScreen
import com.v2ray.app.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Locations : Screen("locations")
    object Settings : Screen("settings")
    object Logs : Screen("logs")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToLocations = { navController.navigate(Screen.Locations.route) },
                onNavigateToLogs = { navController.navigate(Screen.Logs.route) }
            )
        }
        composable(Screen.Locations.route) {
            LocationListScreen(
                viewModel = viewModel,
                onProfileSelected = { /* انتخاب پروفایل */ }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = viewModel)
        }
        composable(Screen.Logs.route) {
            LogViewerScreen(viewModel = viewModel)
        }
    }
}
