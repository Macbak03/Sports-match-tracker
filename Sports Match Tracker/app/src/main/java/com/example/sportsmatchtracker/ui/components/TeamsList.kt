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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun TeamsListItem(
    team: Team,
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = team.city,
                color = Color.LightGray,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = team.name,
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium
            )

        }


        Column(
        ) {
            Text(
                text = "Next match",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "next match",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
        ) {
            Text(
                text = "Last 5",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "W W W W W",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun LeagueTeamsCard(
    leagueName: String,
    teams: List<Team>,
    onTeamClick: (Team) -> Unit,
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
            teams.forEachIndexed { index, match ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.7f),
                        thickness = 0.5.dp
                    )
                }
                TeamsListItem(
                    team = match,
                    onClick = { onTeamClick(match) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TeamListItemPreview() {
    SportsMatchTrackerTheme {
        TeamsListItem(team = Team(name = "Team A", city = "City A", league = League(name = "League A", country = "Country A", sport = Sport("Football"))), onClick = {})
    }
}