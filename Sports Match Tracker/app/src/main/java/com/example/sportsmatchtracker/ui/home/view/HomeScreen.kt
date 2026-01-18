package com.example.sportsmatchtracker.ui.home.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val matches by viewModel.searchResults.collectAsState(initial = emptyList())
    val tabItems by viewModel.tabItems.collectAsState()
    var selectedSport by remember { mutableStateOf<Sport?>(null) }
    var selectedMatch by remember { mutableStateOf<Match?>(null) }

    val filteredMatches = remember(matches, selectedSport) {
        if (selectedSport == null) {
            matches
        } else {
            matches.filter { it.league.sport.name == selectedSport!!.name }
        }
    }

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectPreview() {
    SportsMatchTrackerTheme {
        HomeScreen(viewModel = HomeViewModel(MatchesRepository()))
    }
}