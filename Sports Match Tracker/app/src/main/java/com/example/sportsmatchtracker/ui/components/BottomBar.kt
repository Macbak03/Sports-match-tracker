package com.example.sportsmatchtracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun BottomBar(
    currentRoute: String?,
    onNavigateToHome: () -> Unit,
    onNavigateToTeams: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToTables: () -> Unit
) {
    NavigationBar {
        val items = listOf(
            BottomNavItem("home", "Home", Icons.Default.Home, onNavigateToHome),
            BottomNavItem("teams", "Teams", Icons.Default.Person, onNavigateToTeams),
            BottomNavItem("favourites", "Favourites", Icons.Default.Star, onNavigateToFavorites),
            BottomNavItem("tables", "Tables", Icons.Default.TableChart, onNavigateToTables)
        )

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = item.onClick
            )
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
