package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.match.MatchStatus
import com.example.sportsmatchtracker.ui.tables.view.TablesScreen
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme
import java.time.LocalDateTime
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsBottomSheet(
    match: Match,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = match.league,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            when (match.status) {
                MatchStatus.LIVE -> {
                    LiveMatchContent(match)
                }
                MatchStatus.SCHEDULED -> {
                    ScheduledMatchContent(match)
                }
                MatchStatus.FINISHED -> {
                    FinishedMatchContent(match)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LiveMatchContent(match: Match) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.status.label,
                style = MaterialTheme.typography.titleSmall,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            match.gameTime?.let {
                Text(
                    text = "$it'",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.homeTeam,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${match.homeScore} - ${match.awayScore}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Text(
                text = match.awayTeam,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MatchEvents(match.events)
    }
}

@Composable
private fun ScheduledMatchContent(match: Match) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = match.homeTeam,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = match.formattedDate,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Text(
                text = match.formattedTime,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        
        Text(
            text = match.awayTeam,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun FinishedMatchContent(match: Match) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = match.status.label,
            style = MaterialTheme.typography.titleSmall,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.homeTeam,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = "${match.homeScore} - ${match.awayScore}",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Text(
                text = match.awayTeam,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        MatchEvents(match.events)
    }
}

@Composable
private fun MatchEvents(events: List<MatchEvent>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        events.forEach { event ->
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = event.gameTime,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = event.event,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavouritesPreview() {
    SportsMatchTrackerTheme {
        MatchDetailsBottomSheet(match = Match(
            homeTeam = "Manchester United",
            awayTeam = "Liverpool",
            homeScore = 1,
            awayScore = 3,
            matchDateTime = LocalDateTime.now().minusMinutes(45),
            league = "Premier League",
            events = listOf(
                MatchEvent("35\'", "Red card for Liverpool"),
                MatchEvent("40\'", "Goal for Manchester United")
            )
        ) , onDismiss = {})

    }
}