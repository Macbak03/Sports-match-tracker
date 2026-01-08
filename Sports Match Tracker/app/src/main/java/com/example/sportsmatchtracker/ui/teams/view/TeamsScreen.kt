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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.repository.teams.TeamsRepository
import com.example.sportsmatchtracker.ui.components.LeagueTeamsCard
import com.example.sportsmatchtracker.ui.components.TabSelector
import com.example.sportsmatchtracker.ui.teams.view_model.TeamsViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel
) {
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val teams by viewModel.teams.collectAsState()
    val tabItems by viewModel.tabItems.collectAsState()
    var selectedSport by remember { mutableStateOf<Sport?>(null) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

    val filteredTeams = remember(teams, selectedSport) {
        if (selectedSport == null) {
            teams
        } else {
            teams.filter { it.league.sport.name == selectedSport!!.name }
        }
    }

    Column() {
        if (tabItems.isNotEmpty()) {
            TabSelector(
                tabs = tabItems,
                selectedTab = selectedSport ?: tabItems.first().value,
                onTabSelected = {sport ->
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
            val grouped = filteredTeams.groupBy { it.league }
            grouped.forEach { (league, teams) ->
                item {
                    LeagueTeamsCard(
                        leagueName = league.name,
                        sportName = league.sport.name,
                        teams = teams,
                        onTeamClick = { team -> selectedTeam = team },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TeamsPreview() {
    SportsMatchTrackerTheme {
        TeamsScreen(viewModel = TeamsViewModel(TeamsRepository()))
    }
}