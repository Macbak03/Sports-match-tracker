package com.example.sportsmatchtracker.model.match


data class Match(
    val status: MatchStatus = MatchStatus.FINISHED,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val date: String,
    val league: String,
    val gameTime: String? = null,
    val events: List<MatchEvent> = listOf()
)