package com.dergoogler.modconf.mmrl_wpd.navigation.graphs

import com.dergoogler.modconf.mmrl_wpd.screens.passwords.PasswordsScreen
import com.dergoogler.modconf.mmrl_wpd.navigation.MainScreen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation

enum class PasswordsScreen(val route: String) {
    Home("Settings"),
}

fun NavGraphBuilder.passwordsScreen(
    navController: NavController
) = navigation(
    startDestination = PasswordsScreen.Home.route,
    route = MainScreen.Settings.route
) {
    composable(
        route = PasswordsScreen.Home.route,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        PasswordsScreen(
            navController = navController
        )
    }
}