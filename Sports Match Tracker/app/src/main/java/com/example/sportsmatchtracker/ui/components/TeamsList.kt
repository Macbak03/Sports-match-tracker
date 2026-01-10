package com.example.sportsmatchtracker.ui.components


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@Composable
fun TeamsListItem(
    team: Team,
    onClick: () -> Unit,
    onFavouriteCLick: () -> Unit,
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

        //Spacer(modifier = Modifier.width(16.dp))

//        Column(
//        ) {
//            Text(
//                text = "Last 5",
//                color = Color.LightGray,
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Text(
//                text = "W W W W W",
//                color = Color.Black,
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }

        IconButton(
            onClick = onFavouriteCLick ,
            modifier = Modifier.offset(x = 12.dp)
        ) {
            Icon(
                imageVector = if (team.isSubscribed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (team.isSubscribed) "Unsubscribe league" else "Subscribe league",
                Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LeagueTeamsCard(
    league: League,
    onTeamClick: (Team) -> Unit,
    onFavouriteLeagueClick: () -> Unit,
    onFavouriteTeamClick: (Team) -> Unit,
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
        border = BorderStroke(
            width = 0.5.dp,
            color = Color.LightGray.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = league.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.weight(2f)
                )

                IconButton(
                    onClick = onFavouriteLeagueClick,
                    modifier = Modifier.offset(x = 12.dp)
                ) {
                    Icon(
                        imageVector = if (league.isSubscribed) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (league.isSubscribed) "Unsubscribe league" else "Subscribe league"
                    )
                }
            }


            // Matches with dividers
            league.teams.forEachIndexed { index, team ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.7f),
                        thickness = 0.5.dp
                    )
                }
                TeamsListItem(
                    team = team,
                    onClick = { onTeamClick(team) },
                    onFavouriteCLick = { onFavouriteTeamClick(team) },
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
        LeagueTeamsCard(
            league = League(
                name = "Premier League",
                country = "England",
                sport = Sport(
                    name = "Football"
                ),
                listOf(
                    Team(
                        name = "Manchester City",
                        city = "Manchester",
                    ),
                    Team(
                        name = "Manchester United",
                        city = "Manchester",
                    )
                )
            ),
            onTeamClick = {},
            onFavouriteLeagueClick = {},
            onFavouriteTeamClick = {}
        )
    }
}