package com.example.linebreak.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Game : Screen("game")
    data object Result : Screen("result")
    data object Settings : Screen("settings")
    data object About : Screen("about")
}
