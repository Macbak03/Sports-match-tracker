package com.example.sportsmatchtracker.model.match

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Match(
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val matchDateTime: LocalDateTime,
    val league: String,
    val events: List<MatchEvent> = listOf()
) {
    val status: MatchStatus
        get() {
            val now = LocalDateTime.now()
            val minutesSinceStart = ChronoUnit.MINUTES.between(matchDateTime, now)
            
            return when {
                minutesSinceStart < 0 -> MatchStatus.SCHEDULED
                minutesSinceStart <= 90 -> MatchStatus.LIVE
                else -> MatchStatus.FINISHED
            }
        }
    
    val gameTime: Int?
        get() {
            if (status == MatchStatus.LIVE) {
                val now = LocalDateTime.now()
                return ChronoUnit.MINUTES.between(matchDateTime, now).toInt()
            }
            return null
        }
    
    val formattedDate: String
        get() = matchDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    
    val formattedTime: String
        get() = matchDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    
    val formattedDateTime: String
        get() = matchDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
}