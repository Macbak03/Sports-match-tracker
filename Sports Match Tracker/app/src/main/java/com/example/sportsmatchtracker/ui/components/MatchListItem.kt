package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.model.match.Match

@Composable
fun MatchListItem(match: Match, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .background(color = Color(0xFF1E293B), shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = match.date,
            modifier = Modifier.width(90.dp),
            color = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = match.homeTeam,
                color = Color.White
            )
            Text(
                text = match.awayTeam,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
        ) {
            Text(
                text = match.homeScore.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = match.awayScore.toString(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
}
    }


@Composable
fun CharacterHeader(
    char: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1E293B)
) {
    Text(
        text = char,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(color = color, shape = RoundedCornerShape(8.dp)),
        textAlign = TextAlign.Center
    )
}
