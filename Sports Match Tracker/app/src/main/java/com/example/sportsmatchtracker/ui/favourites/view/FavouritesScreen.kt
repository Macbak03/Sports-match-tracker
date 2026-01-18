package com.example.sportsmatchtracker.ui.favourites.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.ui.components.LeagueMatchCard
import com.example.sportsmatchtracker.ui.components.MatchDetailsBottomSheet
import com.example.sportsmatchtracker.ui.components.TabSelector
import com.example.sportsmatchtracker.ui.favourites.view_model.FavouritesViewModel
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun FavouritesScreen(
    viewModel: FavouritesViewModel,
    searchQuery: String
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val teamResults by viewModel.teamSearchResults.collectAsState()
    val leagueResults by viewModel.leagueSearchResults.collectAsState()
    val teamMatches by viewModel.teamMatches.collectAsState()
    val leagueMatches by viewModel.leagueMatches.collectAsState()

    val teamSubscriptions =
        if (searchQuery.isBlank())
            viewModel.teamsSubscriptions.collectAsState().value
        else
            teamResults

    val leagueSubscriptions =
        if (searchQuery.isBlank())
            viewModel.leaguesSubscriptions.collectAsState().value
        else
            leagueResults


    var selectedMatch by remember { mutableStateOf<Match?>(null) }

    var selectedTab: String? by remember { mutableStateOf("teams")}

    val matches = if (selectedTab == "teams") teamMatches else leagueMatches

    // Fetch matches when lists are ready and selected tab changes
    LaunchedEffect(selectedTab, teamSubscriptions, leagueSubscriptions) {
        if (selectedTab == "teams" && teamSubscriptions.isNotEmpty()) {
            viewModel.fetchTeamMatches()
        } else if (selectedTab == "leagues" && leagueSubscriptions.isNotEmpty()) {
            viewModel.fetchLeagueMatches()
        }
    }
    LaunchedEffect(searchQuery, selectedTab) {
        viewModel.search(searchQuery)
    }

    Column() {
        if (viewModel.tabItems.isNotEmpty()) {
            TabSelector(
                tabs = viewModel.tabItems,
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    viewModel.setTab(tab ?: "teams")
                }
            )

            Spacer(modifier = Modifier.padding(8.dp))
        }

        if ((selectedTab == "teams" && teamSubscriptions.isEmpty()) || 
            (selectedTab == "leagues" && leagueSubscriptions.isEmpty())) {
            Text(
                text = if (selectedTab == "teams") "No teams subscriptions" else "No league subscriptions",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            val filteredMatches = if (searchQuery.isBlank()) {
                matches
            } else {
                if (selectedTab == "teams") {
                    matches.filter { match ->
                        teamResults.any { team ->
                            match.homeTeam == team.teamName||
                                    match.awayTeam == team.teamName
                        }
                    }
                } else {
                    matches.filter { match ->
                        leagueResults.any { league ->
                            match.league.name == league.leagueName
                        }
                    }
                }
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 10.dp)
            ) {
                val grouped = if (selectedTab == "teams") {
                    val subscribedTeamNames = teamSubscriptions.map { it.teamName }.toSet()

                    filteredMatches.flatMap { match ->
                        buildList {
                            if (match.homeTeam in subscribedTeamNames) {
                                add(match.homeTeam to match)
                            }
                            if (match.awayTeam in subscribedTeamNames) {
                                add(match.awayTeam to match)
                            }
                        }
                    }.groupBy({ it.first }, { it.second })
                } else {
                    val subscribedLeagues = leagueSubscriptions.map { it.leagueName to it.leagueCountry }.toSet()

                    filteredMatches
                        .filter { match ->
                            (match.league.name to match.league.country) in subscribedLeagues
                        }
                        .groupBy { it.league.name }
                }

                if(grouped.isNotEmpty()) {
                    grouped.forEach { (key, teamMatches) ->
                        item {
                            LeagueMatchCard(
                                leagueName = key,
                                matches = teamMatches,
                                onMatchClick = { match -> selectedMatch = match },
                                modifier = Modifier.fillParentMaxWidth()
                            )
                        }
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
}