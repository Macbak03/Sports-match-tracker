package com.example.sportsmatchtracker.ui.teams.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.repository.teams.TeamsRepository
import com.example.sportsmatchtracker.ui.components.MatchDetailsBottomSheet
import com.example.sportsmatchtracker.ui.components.LeagueTeamsCard
import com.example.sportsmatchtracker.ui.teams.view_model.TeamsViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun TeamsScreen(
    viewModel: TeamsViewModel
) {
    val teams by viewModel.teams.collectAsState()
    viewModel.getTeams()
    var selectedTeam by remember { mutableStateOf<Team?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 10.dp)
    ) {
        val grouped = teams.groupBy { it.league }

        grouped.forEach { (league, teams) ->
            item {
                LeagueTeamsCard(
                    leagueName = league.name,
                    teams = teams,
                    onTeamClick = { team -> selectedTeam = team },
                    modifier = Modifier.fillParentMaxWidth()
                )
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