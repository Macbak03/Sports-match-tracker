package com.example.sportsmatchtracker.ui.home.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.Home.HomeRepository
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.ui.auth.view.AuthScreen
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.components.CharacterHeader
import com.example.sportsmatchtracker.ui.components.MatchListItem
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun HomeScreen(
    user: User,
    viewModel: HomeViewModel
) {
    val matches by viewModel.matches.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .align(Alignment.TopCenter)
                .padding(top = 120.dp)
        ) {
            val grouped = matches.groupBy { it.league }

            grouped.forEach { (initial, matches) ->
                item{
                    CharacterHeader(char = initial, Modifier.fillParentMaxWidth())
                }
                items(matches) { match ->
                    MatchListItem(
                        match = match,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConnectPreview() {
    SportsMatchTrackerTheme {
        HomeScreen(user = User(), viewModel = HomeViewModel(HomeRepository()))
    }
}


