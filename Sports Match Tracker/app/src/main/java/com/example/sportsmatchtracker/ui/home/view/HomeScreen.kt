package com.example.sportsmatchtracker.ui.home.view

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
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.repository.home.HomeRepository
import com.example.sportsmatchtracker.ui.components.LeagueMatchCard
import com.example.sportsmatchtracker.ui.components.MatchDetailsBottomSheet
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val matches by viewModel.matches.collectAsState()
    var selectedMatch by remember { mutableStateOf<Match?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 10.dp)
    ) {
        val grouped = matches.groupBy { it.league }

        grouped.forEach { (leagueName, leagueMatches) ->
            item {
                LeagueMatchCard(
                    leagueName = leagueName,
                    matches = leagueMatches,
                    onMatchClick = { match -> selectedMatch = match },
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }
    }

    selectedMatch?.let { match ->
        MatchDetailsBottomSheet(
            match = match,
            onDismiss = { selectedMatch = null }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectPreview() {
    SportsMatchTrackerTheme {
        HomeScreen(viewModel = HomeViewModel(HomeRepository()))
    }
}


