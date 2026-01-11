package com.example.sportsmatchtracker.ui.components

import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchResult
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme
import com.example.sportsmatchtracker.ui.theme.green40

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailsBottomSheet(
    team: Team,
    onDismiss: () -> Unit,
    fetchLastMatchResults: suspend (Team) -> List<MatchResult> = { emptyList() },
    fetchNextMatch: suspend (Team) -> Match? = { null }
) {
    var teamLastResults by remember(team) {
        mutableStateOf(emptyList<MatchResult>())
    }

    var teamNextMatch by remember(team) {
        mutableStateOf<Match?>(null)
    }

    LaunchedEffect(team) {
        teamLastResults = fetchLastMatchResults(team)
        teamNextMatch = fetchNextMatch(team)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {

            Column() {
                Row() {
                    Text(
                        text = team.city,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = teamNextMatch?.league?.name ?: "" ,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = team.name,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row() {
                        if (teamLastResults.isEmpty()) {
                            Text(
                                text = "No recent matches",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.LightGray,
                            )
                        } else {
                            teamLastResults.forEach { result ->
                                Icon(
                                    imageVector = when (result) {
                                        MatchResult.WIN -> Icons.Rounded.CheckCircle
                                        MatchResult.DRAW -> Icons.Rounded.RemoveCircle
                                        MatchResult.LOSE -> Icons.Rounded.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when (result) {
                                        MatchResult.WIN -> green40
                                        MatchResult.DRAW -> Color.Gray
                                        MatchResult.LOSE -> MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }
                    }
                }


            }

            Spacer(modifier = Modifier.height(16.dp))

            val match = teamNextMatch
//            val match = Match(
//                homeTeam = "FC Barcelona",
//                awayTeam = "Real Madrid",
//                homeScore = 0,
//                awayScore = 0,
//                matchDateTime = java.time.Instant.now(),
//                league = com.example.sportsmatchtracker.model.league.League(
//                    name = "Premier League",
//                    country = "England",
//                    sport = com.example.sportsmatchtracker.model.sport.Sport(
//                        name = "Football"
//                    ),
//                    teams = listOf()
//                ),
//                events = listOf(),
//                matchStadium = "Old Trafford",
//                seasonStartDate = java.time.LocalDate.now(),
//                seasonEndDate = java.time.LocalDate.now()
//            )

            Column() {
                Row() {
                    Text(
                        text = "Next match with",
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium
                    )

                }

                if (match != null) {
                    Text(
                        text = if (match.awayTeam == team.name) match.homeTeam else match.awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Column() {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = match.formattedDate,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )
                            Text(
                                text = match.formattedTime,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )


                            Text(
                                text = match.matchStadium,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.LightGray
                            )

                        }
                    }
                } else {
                    Text(
                        text = "No planned matches",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                }
            }

        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TeamDetailsBottomSheetPreview() {
    SportsMatchTrackerTheme {
        TeamDetailsBottomSheet(
            team = Team(
                name = "FC Barcelona",
                city = "Barcelona"
            ),
            onDismiss = {}
        )
    }
}