package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchStatus

@Composable
fun MatchListItem(
    match: Match, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column() {
            Text(
                text = match.status.label,
                color = if (match.status == MatchStatus.LIVE) Color.Red else Color.Gray
            )
            Text(
                text = match.formattedDate,
                modifier = Modifier.width(90.dp),
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = match.homeTeam,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = match.awayTeam,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
        ) {
            Text(
                text = match.homeScore.toString(),
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = match.awayScore.toString(),
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun LeagueMatchCard(
    leagueName: String,
    matches: List<Match>,
    onMatchClick: (Match) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                text = leagueName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Start
            )
            
            // Matches with dividers
            matches.forEachIndexed { index, match ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.7f),
                        thickness = 0.5.dp
                    )
                }
                MatchListItem(
                    match = match,
                    onClick = { onMatchClick(match) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}