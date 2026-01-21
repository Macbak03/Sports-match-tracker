package com.example.sportsmatchtracker.ui.home.view

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.repository.matches.MatchesRepository
import com.example.sportsmatchtracker.ui.components.LeagueMatchCard
import com.example.sportsmatchtracker.ui.components.TabSelector
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.components.MatchDetailsBottomSheet
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme
import com.example.sportsmatchtracker.ui.components.AddMatchDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.rememberCoroutineScope

import java.time.ZoneId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val matches by viewModel.searchResults.collectAsState(initial = emptyList())
    val tabItems by viewModel.tabItems.collectAsState()
    val user by viewModel.user.collectAsState(initial = null)
    
    var selectedSport by remember { mutableStateOf<Sport?>(null) }
    var selectedMatch by remember { mutableStateOf<Match?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    // Add Match Dialog State
    var showAddMatchDialog by remember { mutableStateOf(false) }
    val leagues by viewModel.availableLeagues.collectAsState(initial = emptyList())
    val buildings by viewModel.addMatchBuildings.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val filteredMatches = remember(matches, selectedSport) {
        if (selectedSport == null) {
            matches
        } else {
            matches.filter { it.league.sport.name == selectedSport!!.name }
        }
    }

    Scaffold(
        floatingActionButton = {
            if (user?.role == "admin") {
                FloatingActionButton(
                    onClick = { 
                        viewModel.loadAddMatchData()
                        showAddMatchDialog = true 
                    }
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Match")
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
                    val grouped = filteredMatches.groupBy { it.league }

                    grouped.forEach { (league, leagueMatches) ->
                        item {
                            LeagueMatchCard(
                                leagueName = league.name,
                                matches = leagueMatches,
                                onMatchClick = { match -> selectedMatch = match },
                                modifier = Modifier.fillParentMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddMatchDialog) {
        AddMatchDialog(
            onDismissRequest = { showAddMatchDialog = false },
            onConfirmation = { home, away, date, league, building ->
                scope.launch {
                    val season = viewModel.getSeasonForDate(league, date.toLocalDate())
                    if (season != null) {
                        val match = Match(
                            homeTeam = home,
                            awayTeam = away,
                            homeScore = 0,
                            awayScore = 0,
                            matchDateTime = date.atZone(ZoneId.of("Europe/Warsaw")).toInstant(),
                            league = league,
                            matchStadium = building,
                            seasonStartDate = season.dateStart,
                            seasonEndDate = season.dateEnd
                        )
                        viewModel.insertMatch(
                            match = match,
                            onSuccess = {
                                showAddMatchDialog = false
                            },
                            onError = { /* Handle error */ }
                        )
                    } else {
                        // Handle no season found
                    }
                }
            },
            availableLeagues = leagues,
            availableBuildings = buildings
        )
    }

    selectedMatch?.let { match ->
        MatchDetailsBottomSheet(
            match = match,
            onDismiss = { selectedMatch = null },
            onFetchEvents = { match ->
               viewModel.fetchMatchEvents(match)
            }
        )
    }
}