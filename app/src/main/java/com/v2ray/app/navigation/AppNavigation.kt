package com.v2ray.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.v2ray.app.ui.dashboard.DashboardScreen
import com.v2ray.app.ui.location.LocationListScreen
import com.v2ray.app.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Locations : Screen("locations")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Logs : Screen("logs")
    object Admin : Screen("admin")
    object Splash : Screen("splash")
}

@Composable
fun AppNavigation(
    viewModel: MainViewModel,
    onAdminClick: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToLocations: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToLogs: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = viewModel,
                onConnect = { /* connect logic */ },
                onDisconnect = { /* disconnect logic */ }
            )
        }
        composable(Screen.Locations.route) {
            LocationListScreen(
                viewModel = viewModel,
                onProfileSelected = { /* select profile */ }
            )
        }
        composable(Screen.Settings.route) {
            // SettingsScreen()
        }
        composable(Screen.About.route) {
            // AboutScreen()
        }
        composable(Screen.Logs.route) {
            // LogViewerScreen()
        }
        composable(Screen.Admin.route) {
            // AdminScreen()
        }
        composable(Screen.Splash.route) {
            // SplashScreen()
        }
    }
}
