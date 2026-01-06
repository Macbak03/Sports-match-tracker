package com.example.sportsmatchtracker.navigation

sealed class Screen(val route: String) {
    object Connection : Screen("connection")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Settings: Screen("settings")


}
