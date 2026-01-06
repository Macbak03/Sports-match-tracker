package com.example.sportsmatchtracker.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.sportsmatchtracker.ui.auth.view.AuthScreen
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.network.view.ConnectionScreen
import com.example.sportsmatchtracker.ui.home.view.HomeScreen
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.network.view_model.ConnectionViewModel

@Composable
fun AppNavigation(
    connectionViewModel: ConnectionViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel
) {
    val navController = rememberNavController()
    val clientState by connectionViewModel.uiState.collectAsState()
    val user by authViewModel.user.collectAsState()

    // Handle navigation based on connection and authentication state
    LaunchedEffect(clientState.connected, user) {
        when {
            // Not connected -> stay on/go to connection screen
            !clientState.connected -> {
                if (navController.currentDestination?.route != Screen.Connection.route) {
                    navController.navigate(Screen.Connection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            // Connected but not authenticated -> go to auth screen
            user == null -> {
                if (navController.currentDestination?.route != Screen.Auth.route) {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Connection.route) { inclusive = true }
                    }
                }
            }
            // Connected and authenticated -> go to home screen
            else -> {
                if (navController.currentDestination?.route != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Connection.route
    ) {
        composable(Screen.Connection.route) {
            ConnectionScreen(viewModel = connectionViewModel)
        }

        composable(Screen.Auth.route) {
            AuthScreen(viewModel = authViewModel)
        }

        composable(Screen.Home.route) {
            user?.let {
                HomeScreen(
                    user = it,
                    viewModel = homeViewModel
                )
            }
        }
    }
}
