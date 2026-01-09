package com.example.sportsmatchtracker.ui.teams.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.components.LeagueTeamsCard
import com.example.sportsmatchtracker.ui.components.TabSelector
import com.example.sportsmatchtracker.ui.teams.view_model.TeamsViewModel

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val leagues by viewModel.leagues.collectAsState()
    val tabItems by viewModel.tabItems.collectAsState()
    var selectedSport by remember { mutableStateOf<Sport?>(null) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

    val filteredTeams = remember(leagues, selectedSport) {
        if (selectedSport == null) {
            leagues
        } else {
            leagues.filter { it.sport.name == selectedSport!!.name }
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
            val grouped = filteredTeams.groupBy { it }
            grouped.forEach { (league, _) ->
                item {
                    LeagueTeamsCard(
                        league = league,
                        onTeamClick = { team -> selectedTeam = team },
                        onFavouriteLeagueClick = {
                            if (league.isSubscribed)
                                viewModel.unsubscribeLeague(league.toSubscription())
                            else viewModel.subscribeLeague(league.toSubscription())
                        },
                        onFavouriteTeamClick = { team ->
                            if (team.isSubscribed)
                                viewModel.unsubscribeTeam(team.toSubscription())
                            else viewModel.subscribeTeam(team.toSubscription())
                        },
                        modifier = Modifier.fillParentMaxWidth()
                    )
                }
            }
        }
    }

//    selectedTeam?.let { team ->
//        MatchDetailsBottomSheet(
//            team = team,
//            onDismiss = { selectedTeam = null }
//        )
//    }
}
