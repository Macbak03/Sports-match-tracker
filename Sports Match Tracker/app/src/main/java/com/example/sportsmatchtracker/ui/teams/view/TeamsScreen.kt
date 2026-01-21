package com.example.sportsmatchtracker.ui.teams.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.components.LeagueTeamsCard
import com.example.sportsmatchtracker.ui.components.TabSelector
import com.example.sportsmatchtracker.ui.components.TeamDetailsBottomSheet
import com.example.sportsmatchtracker.ui.components.AddTeamDialog
import com.example.sportsmatchtracker.ui.teams.view_model.TeamsViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel,
    searchQuery: String
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val leagues by viewModel.leagues.collectAsState()
    val tabItems by viewModel.tabItems.collectAsState()
    val user by viewModel.user.collectAsState(initial = null)

    var selectedSport by remember { mutableStateOf<Sport?>(null) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Add Team Dialog State
    var showAddTeamDialog by remember { mutableStateOf(false) }

    val filteredLeagues = remember(leagues, searchResults, selectedSport, searchQuery) {
        val leaguesToDisplay = if (searchQuery.isBlank()) {
            leagues
        } else {
            searchResults
        }

        leaguesToDisplay.filter { league ->
            selectedSport == null || league.sport == selectedSport
        }
    }

    Scaffold(
        floatingActionButton = {
            if (user?.role == "admin") {
                FloatingActionButton(
                    onClick = { 
                        showAddTeamDialog = true 
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Team")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            }
        ) {
            Column() {
                if (tabItems.isNotEmpty()) {
                    TabSelector(
                        tabs = tabItems,
                        selectedTab = selectedSport ?: tabItems.first().value,
                        onTabSelected = { sport ->
                            selectedSport = if (sport == tabItems.firstOrNull()?.value) {
                                null
                            } else {
                                sport
                            }
                        }
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(all = 10.dp)
                ) {
                    filteredLeagues.forEach { league ->
                        item {
                            LeagueTeamsCard(
                                league = league,
                                onTeamClick = { team -> selectedTeam = team },
                                onFavouriteLeagueClick = {
                                    if (league.isSubscribed)
                                        viewModel.unsubscribeLeague(league.toSubscription())
                                    else
                                        viewModel.subscribeLeague(league.toSubscription())
                                },
                                onFavouriteTeamClick = { team ->
                                    if (team.isSubscribed)
                                        viewModel.unsubscribeTeam(team.toSubscription())
                                    else
                                        viewModel.subscribeTeam(team.toSubscription())
                                },
                                modifier = Modifier.fillParentMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTeamDialog) {
        AddTeamDialog(
            onDismissRequest = { showAddTeamDialog = false },
            onConfirmation = { name, city, league, onError ->
                viewModel.addTeam(
                    team = Team(name = name, city = city),
                    league = league,
                    onSuccess = { showAddTeamDialog = false },
                    onError = { error -> onError(error) }
                )
            },
            availableLeagues = leagues
        )
    }

    selectedTeam?.let { team ->
        TeamDetailsBottomSheet(
            team = team,
            onDismiss = { selectedTeam = null },
            fetchLastMatchResults = { team ->
                viewModel.getLastMatchesResults(team.name)
            },
            fetchNextMatch = { team ->
                viewModel.getNextMatch(team.name)
            }
        )
    }
}

