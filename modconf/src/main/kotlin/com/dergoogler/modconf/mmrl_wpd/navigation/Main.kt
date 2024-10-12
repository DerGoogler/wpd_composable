package com.dergoogler.modconf.mmrl_wpd.navigation

enum class MainScreen(
    val route: String,
    val label: String,
) {
    Passwords(
        route = "PasswordsScreen",
        label = "Passwords",
    ),

    Settings(
        route = "SettingsScreen",
        label = "Settings",
    ),
}