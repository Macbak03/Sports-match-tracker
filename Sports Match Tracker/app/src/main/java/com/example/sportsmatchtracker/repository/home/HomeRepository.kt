package com.example.sportsmatchtracker.repository.home

import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.match.MatchStatus
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class HomeRepository : Repository() {
    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matchesState: StateFlow<List<Match>> = _matchesState.asStateFlow()

    init {
        val now = LocalDateTime.now()
        
        _matchesState.value = listOf(
            // LIVE matches (started less than 90 minutes ago)
            Match(
                homeTeam = "Real Madrid",
                awayTeam = "Barcelona",
                homeScore = 2,
                awayScore = 1,
                matchDateTime = now.minusMinutes(81),
                league = "La Liga",
                events = listOf(
                    MatchEvent("80'", "Goal for Real Madrid")
                )
            ),
            Match(
                homeTeam = "Manchester United",
                awayTeam = "Liverpool",
                homeScore = 1,
                awayScore = 3,
                matchDateTime = now.minusMinutes(45),
                league = "Premier League",
                events = listOf(
                    MatchEvent("70'", "Red card for Liverpool"),
                    MatchEvent("90'", "Goal for Manchester United")
                )
            ),
            // SCHEDULED matches (future)
            Match(
                homeTeam = "Bayern Munich",
                awayTeam = "Borussia Dortmund",
                homeScore = 0,
                awayScore = 0,
                matchDateTime = now.plusDays(2).withHour(20).withMinute(0),
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Juventus",
                awayTeam = "AC Milan",
                homeScore = 0,
                awayScore = 0,
                matchDateTime = now.plusDays(3).withHour(18).withMinute(30),
                league = "Serie A"
            ),
            // FINISHED matches (started more than 90 minutes ago)
            Match(
                homeTeam = "PSG",
                awayTeam = "Lyon",
                homeScore = 3,
                awayScore = 0,
                matchDateTime = now.minusHours(3),
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Chelsea",
                awayTeam = "Arsenal",
                homeScore = 1,
                awayScore = 1,
                matchDateTime = now.minusDays(1).withHour(15).withMinute(0),
                league = "Premier League"
            ),
            Match(
                homeTeam = "Atletico Madrid",
                awayTeam = "Sevilla",
                homeScore = 2,
                awayScore = 0,
                matchDateTime = now.minusDays(2).withHour(21).withMinute(0),
                league = "La Liga"
            ),
            Match(
                homeTeam = "Napoli",
                awayTeam = "Inter",
                homeScore = 0,
                awayScore = 2,
                matchDateTime = now.minusDays(3).withHour(19).withMinute(45),
                league = "Serie A"
            ),
            Match(
                homeTeam = "RB Leipzig",
                awayTeam = "Bayer Leverkusen",
                homeScore = 3,
                awayScore = 1,
                matchDateTime = now.minusDays(4).withHour(17).withMinute(30),
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Marseille",
                awayTeam = "Monaco",
                homeScore = 2,
                awayScore = 2,
                matchDateTime = now.minusDays(5).withHour(20).withMinute(0),
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Tottenham",
                awayTeam = "Manchester City",
                homeScore = 1,
                awayScore = 4,
                matchDateTime = now.minusDays(6).withHour(16).withMinute(0),
                league = "Premier League"
            ),
            Match(
                homeTeam = "Valencia",
                awayTeam = "Real Sociedad",
                homeScore = 3,
                awayScore = 2,
                matchDateTime = now.minusDays(7).withHour(22).withMinute(0),
                league = "La Liga"
            ),
            Match(
                homeTeam = "Fiorentina",
                awayTeam = "Lazio",
                homeScore = 2,
                awayScore = 3,
                matchDateTime = now.minusDays(8).withHour(18).withMinute(0),
                league = "Serie A"
            ),
            Match(
                homeTeam = "Schalke",
                awayTeam = "VfB Stuttgart",
                homeScore = 1,
                awayScore = 1,
                matchDateTime = now.minusDays(9).withHour(15).withMinute(30),
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Nice",
                awayTeam = "Rennes",
                homeScore = 0,
                awayScore = 1,
                matchDateTime = now.minusDays(10).withHour(19).withMinute(0),
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Everton",
                awayTeam = "Leicester",
                homeScore = 2,
                awayScore = 0,
                matchDateTime = now.minusDays(11).withHour(14).withMinute(0),
                league = "Premier League"
            ),
            Match(
                homeTeam = "Celta Vigo",
                awayTeam = "Real Betis",
                homeScore = 1,
                awayScore = 2,
                matchDateTime = now.minusDays(12).withHour(21).withMinute(30),
                league = "La Liga"
            ),
            Match(
                homeTeam = "Sampdoria",
                awayTeam = "Torino",
                homeScore = 3,
                awayScore = 1,
                matchDateTime = now.minusDays(13).withHour(17).withMinute(0),
                league = "Serie A"
            ),
            Match(
                homeTeam = "Hertha Berlin",
                awayTeam = "Eintracht Frankfurt",
                homeScore = 0,
                awayScore = 2,
                matchDateTime = now.minusDays(14).withHour(16).withMinute(30),
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Lille",
                awayTeam = "Strasbourg",
                homeScore = 1,
                awayScore = 1,
                matchDateTime = now.minusDays(15).withHour(20).withMinute(45),
                league = "Ligue 1"
            )
        )
    }


}