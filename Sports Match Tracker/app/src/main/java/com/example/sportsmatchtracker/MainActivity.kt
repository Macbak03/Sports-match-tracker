package com.example.sportsmatchtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.sportsmatchtracker.navigation.AppNavigation
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.favourites.view_model.FavouritesViewModel
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.network.view_model.ConnectionViewModel
import com.example.sportsmatchtracker.ui.settings.view_model.SettingsViewModel
import com.example.sportsmatchtracker.ui.tables.view_model.TablesViewModel
import com.example.sportsmatchtracker.ui.teams.view_model.TeamsViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

class MainActivity : ComponentActivity() {
    private val connectionViewModel: ConnectionViewModel by viewModels { ConnectionViewModel.Factory }
    private val authViewModel: AuthViewModel by viewModels { AuthViewModel.Factory }
    private val homeViewModel: HomeViewModel by viewModels { HomeViewModel.Factory }
    private val settingsViewModel: SettingsViewModel by viewModels { SettingsViewModel.Factory }
    private val teamsViewModel: TeamsViewModel by viewModels { TeamsViewModel.Factory }
    private val favouritesViewModel: FavouritesViewModel by viewModels { FavouritesViewModel.Factory }
    private val tablesViewModel: TablesViewModel by viewModels { TablesViewModel.Factory }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportsMatchTrackerTheme {
                AppNavigation(
                    connectionViewModel = connectionViewModel,
                    authViewModel = authViewModel,
                    homeViewModel = homeViewModel,
                    settingsViewModel = settingsViewModel,
                    teamsViewModel = teamsViewModel,
                    favouritesViewModel = favouritesViewModel,
                    tablesViewModel = tablesViewModel
                )
            }
        }
    }
}