package com.tingjizhushou.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tingjizhushou.R
import com.tingjizhushou.ui.screens.admin.AdminLoginScreen
import com.tingjizhushou.ui.screens.history.HistoryScreen
import com.tingjizhushou.ui.screens.home.HomeScreen
import com.tingjizhushou.ui.screens.recording.RecordingScreen
import com.tingjizhushou.ui.screens.result.ResultScreen
import com.tingjizhushou.ui.screens.settings.SettingsScreen
import com.tingjizhushou.ui.screens.subscription.SubscriptionScreen

/**
 * Navigation destinations enum for the app.
 */
sealed class Screen(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector
) {
    data object Home : Screen("home", R.string.title_home, Icons.Filled.Home)
    data object Recording : Screen("recording", R.string.title_recording, Icons.Filled.Mic)
    data object History : Screen("history", R.string.title_history, Icons.Filled.History)
    data object Settings : Screen("settings", R.string.title_settings, Icons.Filled.Settings)
    
    // Result screen with record ID parameter
    companion object {
        const val RESULT_ROUTE = "result/{recordId}"
        const val RECORD_ID_ARG = "recordId"
        
        fun resultRoute(recordId: Long) = "result/$recordId"
    }
}

/**
 * List of bottom navigation items.
 */
val bottomNavItems = listOf(
    Screen.Home,
    Screen.Recording,
    Screen.History,
    Screen.Settings
)

/**
 * Main navigation host composable for the app.
 * Sets up bottom navigation and navigation graph.
 */
@Composable
fun TingJiNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(stringResource(screen.titleResId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Recording.route) {
                RecordingScreen()
            }
            composable(Screen.History.route) {
                HistoryScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable("subscription") {
                SubscriptionScreen(navController = navController)
            }
            composable("admin-login") {
                AdminLoginScreen(navController = navController)
            }
            composable(
                route = Screen.RESULT_ROUTE,
                arguments = listOf(
                    navArgument(Screen.RECORD_ID_ARG) { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong(Screen.RECORD_ID_ARG) ?: 0L
                ResultScreen(
                    recordId = recordId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
