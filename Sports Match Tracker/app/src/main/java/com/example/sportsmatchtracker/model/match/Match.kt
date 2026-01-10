package com.example.sportsmatchtracker.model.match

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import com.example.sportsmatchtracker.model.league.League
import java.time.Instant
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class Match(
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val matchDateTime: Instant,
    val league: League,
    val events: List<MatchEvent> = listOf(),
    val matchStadium: String,
    val seasonStartDate: LocalDate,
    val seasonEndDate: LocalDate
) {
    val status: MatchStatus
        get() {
            val now = Instant.now()
            val minutesSinceStart = Duration.between(matchDateTime, now).toMinutes()

            return when {
                minutesSinceStart < 0 -> MatchStatus.SCHEDULED
                minutesSinceStart <= 90 -> MatchStatus.LIVE
                else -> MatchStatus.FINISHED
            }
        }

    val gameTime: Int?
        get() =
            if (status == MatchStatus.LIVE)
                Duration.between(matchDateTime, Instant.now()).toMinutes().toInt()
            else null

    val formattedDate: String
        get() = matchDateTime
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

    val formattedTime: String
        get() = matchDateTime
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(DateTimeFormatter.ofPattern("HH:mm"))

    fun getResult(teamName: String): MatchResult? {
        if (status != MatchStatus.FINISHED) return null

        if (teamName == homeTeam) {
            return when {
                homeScore < awayScore -> MatchResult.LOSE
                homeScore > awayScore -> MatchResult.WIN
                else -> MatchResult.DRAW

            }
        }

        if (teamName == awayTeam) {
            return when {
                awayScore < homeScore -> MatchResult.LOSE
                awayScore > homeScore -> MatchResult.WIN
                else -> MatchResult.DRAW
            }
        }

        return null
    }
}