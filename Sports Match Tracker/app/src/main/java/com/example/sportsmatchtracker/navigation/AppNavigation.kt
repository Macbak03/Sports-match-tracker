package com.example.sportsmatchtracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sportsmatchtracker.ui.auth.view.AuthScreen
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.components.TopBar
import com.example.sportsmatchtracker.ui.network.view.ConnectionScreen
import com.example.sportsmatchtracker.ui.home.view.HomeScreen
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.network.view_model.ConnectionViewModel
import com.example.sportsmatchtracker.ui.settings.view.SettingsScreen
import com.example.sportsmatchtracker.ui.settings.view_model.SettingsViewModel

@Composable
fun AppNavigation(
    connectionViewModel: ConnectionViewModel,
    authViewModel: AuthViewModel,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val navController = rememberNavController()
    val clientState by connectionViewModel.uiState.collectAsState()
    val user by authViewModel.user.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var searchQuery by remember { mutableStateOf("") }

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
    // Reset search query when route changes
    LaunchedEffect(currentRoute) {
        searchQuery = ""
    }

    val showTopBar = currentRoute in listOf(
        Screen.Home.route,
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                TopBar(
                    currentRoute = currentRoute,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onSearch = {
                        when (currentRoute) {
                            //Screen.Home.route -> homeViewModel.search(searchQuery)
                        }
                    }
                )
            }
        }
    ){ innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Connection.route,
            modifier = Modifier.padding(innerPadding)
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
                        viewModel = homeViewModel,
//                        onNavigateToTeams = {
//                            navController.navigate(Screen.Teams.route)
//                        },
//                        onNavigateToSettings = {
//                            navController.navigate(Screen.Settings.route)
 //                      }
                   )
                }
            }

//            composable(Screen.Teams.route) {
//                user?.let {
//                    TeamsScreen(
//                        onNavigateBack = {
//                            navController.popBackStack()
//                        }
//                    )
//                }
//            }

            composable(Screen.Settings.route) {
                user?.let {
                    SettingsScreen(
                        user = it,
                        viewModel = settingsViewModel
                        //onNavigateBack = {
                            //navController.popBackStack()
                       // }
                    )
                }
            }
        }
    }

//    NavHost(
//        navController = navController,
//        startDestination = Screen.Connection.route
//    ) {
//        composable(Screen.Connection.route) {
//            ConnectionScreen(viewModel = connectionViewModel)
//        }
//
//        composable(Screen.Auth.route) {
//            AuthScreen(viewModel = authViewModel)
//        }
//
//        composable(Screen.Home.route) {
//            user?.let {
//                HomeScreen(
//                    user = it,
//                    viewModel = homeViewModel
//                )
//            }
//        }
//    }
}
