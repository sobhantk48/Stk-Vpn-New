package com.v2ray.app.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.v2ray.app.security.AdminSession
import com.v2ray.app.ui.about.AboutScreen
import com.v2ray.app.ui.admin.AdminLoginScreen
import com.v2ray.app.ui.admin.AdminScreen
import com.v2ray.app.ui.dashboard.DashboardScreen
import com.v2ray.app.ui.groups.GroupsScreen
import com.v2ray.app.ui.location.LocationListScreen
import com.v2ray.app.ui.onboarding.OnboardingScreen
import com.v2ray.app.ui.settings.LogViewerScreen
import com.v2ray.app.ui.settings.SettingsScreen
import com.v2ray.app.ui.splash.SplashScreen
import com.v2ray.app.ui.subscription.SubscriptionScreen
import com.v2ray.app.ui.tools.*
import com.v2ray.app.utils.OnboardingManager
import com.v2ray.app.viewmodel.MainViewModel
import kotlinx.coroutines.launch

object AppRoutes {
    const val ONBOARDING = "onboarding"
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADMIN_LOGIN = "admin_login"
    const val ADMIN = "admin"
    const val SETTINGS = "settings"
    const val LOCATION_LIST = "location_list"
    const val ABOUT = "about"
    const val LOGS = "logs"
    const val SUBSCRIPTIONS = "subscriptions"
    const val GROUPS = "groups"
    const val INTERNET_QUALITY = "internet_quality"
    const val GEOIP = "geoip"
    const val ADBLOCK = "adblock"
    const val TRAFFIC_HISTORY = "traffic_history"
    const val SPEED_TEST = "speed_test"
    const val MULTI_HOP = "multi_hop"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewModel: MainViewModel = hiltViewModel()
    val adminLoggedIn by AdminSession.loggedIn.collectAsState()

    val startDestination = if (OnboardingManager.isFirstLaunch(context)) {
        AppRoutes.ONBOARDING
    } else {
        AppRoutes.SPLASH
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(AppRoutes.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        OnboardingManager.setFirstLaunchDone(context)
                        navController.navigate(AppRoutes.SPLASH) {
                            popUpTo(AppRoutes.ONBOARDING) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(AppRoutes.SPLASH) {
            SplashScreen(
                onFinish = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        composable(AppRoutes.HOME) {
            DashboardScreen(
                nav = navController,
                drawer = drawerState,
                vm = viewModel,
                onAdminClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(
                        if (adminLoggedIn) AppRoutes.ADMIN else AppRoutes.ADMIN_LOGIN
                    )
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.SETTINGS)
                },
                onNavigateToLocations = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.LOCATION_LIST)
                },
                onNavigateToAbout = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.ABOUT)
                },
                onNavigateToLogs = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.LOGS)
                },
                onNavigateToInternetQuality = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.INTERNET_QUALITY)
                },
                onNavigateToGeoIP = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.GEOIP)
                },
                onNavigateToAdBlock = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.ADBLOCK)
                },
                onNavigateToTrafficHistory = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.TRAFFIC_HISTORY)
                },
                onNavigateToSpeedTest = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.SPEED_TEST)
                },
                onNavigateToMultiHop = {
                    scope.launch { drawerState.close() }
                    navController.navigate(AppRoutes.MULTI_HOP)
                }
            )
        }
        composable(AppRoutes.ADMIN_LOGIN) {
            AdminLoginScreen(
                onSuccess = {
                    navController.navigate(AppRoutes.ADMIN) {
                        popUpTo(AppRoutes.ADMIN_LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(AppRoutes.ADMIN) {
            AdminScreen(vm = viewModel, onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.SETTINGS) {
            SettingsScreen(vm = viewModel, onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.LOCATION_LIST) {
            LocationListScreen(vm = viewModel, onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.LOGS) {
            LogViewerScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.SUBSCRIPTIONS) {
            SubscriptionScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.GROUPS) {
            GroupsScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.INTERNET_QUALITY) {
            InternetQualityScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.GEOIP) {
            GeoIPScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.ADBLOCK) {
            AdBlockScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.TRAFFIC_HISTORY) {
            TrafficHistoryScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.SPEED_TEST) {
            SpeedTestScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.MULTI_HOP) {
            MultiHopScreen(onBack = { navController.popBackStack() })
        }
    }
}
