package com.example.sportsmatchtracker.navigation

sealed class Screen(val route: String) {
    object Connection : Screen("connection")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Teams : Screen("teams")
    object Favourites : Screen("favourites")
    object Tables : Screen("tables")
    object Settings: Screen("settings")
}
