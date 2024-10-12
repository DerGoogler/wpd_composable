package com.dergoogler.modconf.mmrl_wpd.navigation

import androidx.annotation.DrawableRes
import com.dergoogler.modconf.mmrl_wpd.R


enum class MainScreen(
    val route: String,
    val label: String,
    @DrawableRes val icon: Int,
    @DrawableRes val iconFilled: Int
) {
    Passwords(
        route = "PasswordsScreen",
        label = "Passwords",
        icon = R.drawable.key,
        iconFilled = R.drawable.key_filled
    ),

    Settings(
        route = "SettingsScreen",
        label = "Settings",
        icon = R.drawable.settings,
        iconFilled = R.drawable.settings_filled
    ),
}