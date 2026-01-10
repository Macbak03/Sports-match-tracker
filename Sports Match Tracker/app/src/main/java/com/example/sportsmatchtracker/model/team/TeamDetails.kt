package com.example.sportsmatchtracker.model.team

import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchResult
import java.time.Instant

data class TeamDetails(
    val nextMatch: Match?,
    val lastMatchesResults: List<MatchResult>
) {
}