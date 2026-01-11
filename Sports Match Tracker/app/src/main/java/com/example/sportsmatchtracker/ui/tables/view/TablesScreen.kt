package com.example.sportsmatchtracker.ui.tables.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
import androidx.lifecycle.viewModelScope
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.sport.SportName
import com.example.sportsmatchtracker.model.table.Season
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.repository.seasons.SeasonsRepository
import com.example.sportsmatchtracker.repository.tables.TablesRepository
import com.example.sportsmatchtracker.ui.components.TableCard
import com.example.sportsmatchtracker.ui.components.TabItem
import com.example.sportsmatchtracker.ui.components.TabSelector
import com.example.sportsmatchtracker.ui.components.TeamDetailsBottomSheet
import com.example.sportsmatchtracker.ui.tables.view_model.TablesViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme
import kotlinx.coroutines.launch

@Composable
fun TablesScreen(
    viewModel: TablesViewModel
) {

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val seasons by viewModel.seasons.collectAsState()
    val sportTabItems by viewModel.sportTabItems.collectAsState()
    val leagueTabItems by viewModel.leagueTabItems.collectAsState()
    val table by viewModel.table.collectAsState()
    var selectedSport by remember { mutableStateOf<Sport?>(Sport(SportName.FOOTBALL)) }
    var selectedLeague by remember { mutableStateOf<League?>(null) }
    var selectedSeason by remember { mutableStateOf<Season?>(null) }
    var selectedTeam by remember { mutableStateOf<Team?>(null) }


    LaunchedEffect(selectedLeague) {
        val selectedLeague = selectedLeague
        if (selectedLeague != null) {
            viewModel.fetchSeasonsForLeague(selectedLeague)
        }
    }
    LaunchedEffect(leagueTabItems) {
        if (leagueTabItems.isNotEmpty() && selectedLeague == null) {
            selectedLeague = leagueTabItems.first().value
        }
    }

    LaunchedEffect(seasons) {
        if (seasons.isNotEmpty()) {
            selectedSeason = seasons.last()
        }
    }

    LaunchedEffect(selectedLeague, selectedSeason, selectedSport) {
        val selectedLeague = selectedLeague
        val selectedSeason = selectedSeason
        if (selectedLeague != null && selectedSeason != null) {
            viewModel.fetchTable(selectedSeason, selectedLeague)
        }
    }

    Column(
    ) {
        if (sportTabItems.isNotEmpty()) {
            TabSelector(
                tabs = sportTabItems as List<TabItem<Sport?>>,
                selectedTab = selectedSport ?: sportTabItems.first().value,
                onTabSelected = { sport ->
                    selectedSport = sport
                    selectedLeague = null
                    selectedSeason = null
                    if (sport != null) {
                        viewModel.viewModelScope.launch {
                            viewModel.setLeagueTabItems(sport.name.label)
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
        }
        if (leagueTabItems.isNotEmpty()) {
            TabSelector(
                tabs = leagueTabItems as List<TabItem<League?>>,
                selectedTab = selectedLeague ?: leagueTabItems.first().value,
                onTabSelected = { league ->
                    selectedLeague = league
                    selectedSeason = null
                }
            )
            Spacer(modifier = Modifier.padding(8.dp))
        }

        TableCard(
            seasons = seasons,
            table = table,
            selectedSeason = selectedSeason,
            onSeasonSelected = { season ->
                selectedSeason = season
            },
            onTeamClick = { team ->
                selectedTeam = team
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    selectedTeam?.let { team ->
        TeamDetailsBottomSheet(
            team = team,
            onDismiss = { selectedTeam = null },
            fetchLastMatchResults = { viewModel.getTeamLastMatchesResults(it.name) },
            fetchNextMatch = { viewModel.getNextTeamMatch(it.name) }
        )
    }
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavouritesPreview() {
    SportsMatchTrackerTheme {
        TablesScreen(viewModel = TablesViewModel(
            leaguesRepository = LeaguesRepository(),
            seasonsRepository = SeasonsRepository(),
            tablesRepository = TablesRepository(),
            matchesRepository = com.example.sportsmatchtracker.repository.matches.MatchesRepository()
        ))
    }
}