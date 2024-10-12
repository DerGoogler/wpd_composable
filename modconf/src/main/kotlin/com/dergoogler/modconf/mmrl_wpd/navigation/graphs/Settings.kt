package com.dergoogler.modconf.mmrl_wpd.navigation.graphs

import com.dergoogler.modconf.mmrl_wpd.screens.settings.SettingsScreen
import com.dergoogler.modconf.mmrl_wpd.navigation.MainScreen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

enum class SettingsScreen(val route: String) {
    Home("Settings"),
}

fun NavGraphBuilder.settingsScreen(
    navController: NavController
) = navigation(
    startDestination = SettingsScreen.Home.route,
    route = MainScreen.Settings.route
) {
    composable(
        route = SettingsScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        SettingsScreen(
            navController = navController
        )
    }
}