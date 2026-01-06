package com.example.sportsmatchtracker.model.match

import java.util.Date
import java.util.UUID

data class Match(
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val date: String,
    val league: String
)