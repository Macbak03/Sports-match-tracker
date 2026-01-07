package com.example.sportsmatchtracker.repository.home

import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.match.MatchStatus
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeRepository : Repository() {
    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matchesState: StateFlow<List<Match>> = _matchesState.asStateFlow()

    init {
        _matchesState.value = listOf(
            Match(
                status = MatchStatus.LIVE,
                homeTeam = "Real Madrid",
                awayTeam = "Barcelona",
                homeScore = 2,
                awayScore = 1,
                date = "10-02-2025",
                gameTime = "81\'",
                league = "La Liga",
                events = listOf(
                    MatchEvent("80\'", "Goal for Real Madrid")
                )
            ),
            Match(
                status = MatchStatus.LIVE,
                homeTeam = "Manchester United",
                awayTeam = "Liverpool",
                homeScore = 1,
                awayScore = 3,
                date = "10-02-2025",
                gameTime = "90+3\'",
                league = "Premier League",
                events = listOf(
                    MatchEvent("70\'", "Red card for Liverpool"),
                    MatchEvent("90\'", "Goal for Manchester United")
                )
            ),
            Match(
                status = MatchStatus.SCHEDULED,
                homeTeam = "Bayern Munich",
                awayTeam = "Borussia Dortmund",
                homeScore = 4,
                awayScore = 2,
                date = "12-02-2025",
                league = "Bundesliga"
            ),
            Match(
                status = MatchStatus.SCHEDULED,
                homeTeam = "Juventus",
                awayTeam = "AC Milan",
                homeScore = 2,
                awayScore = 2,
                date = "13-02-2025",
                league = "Serie A"
            ),
            Match(
                homeTeam = "PSG",
                awayTeam = "Lyon",
                homeScore = 3,
                awayScore = 0,
                date = "14-02-2025",
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Chelsea",
                awayTeam = "Arsenal",
                homeScore = 1,
                awayScore = 1,
                date = "15-02-2025",
                league = "Premier League"
            ),
            Match(
                homeTeam = "Atletico Madrid",
                awayTeam = "Sevilla",
                homeScore = 2,
                awayScore = 0,
                date = "16-02-2025",
                league = "La Liga"
            ),
            Match(
                homeTeam = "Napoli",
                awayTeam = "Inter",
                homeScore = 0,
                awayScore = 2,
                date = "17-02-2025",
                league = "Serie A"
            ),
            Match(
                homeTeam = "RB Leipzig",
                awayTeam = "Bayer Leverkusen",
                homeScore = 3,
                awayScore = 1,
                date = "18-02-2025",
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Marseille",
                awayTeam = "Monaco",
                homeScore = 2,
                awayScore = 2,
                date = "19-02-2025",
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Tottenham",
                awayTeam = "Manchester City",
                homeScore = 1,
                awayScore = 4,
                date = "20-02-2025",
                league = "Premier League"
            ),
            Match(
                homeTeam = "Valencia",
                awayTeam = "Real Sociedad",
                homeScore = 3,
                awayScore = 2,
                date = "21-02-2025",
                league = "La Liga"
            ),
            Match(
                homeTeam = "Fiorentina",
                awayTeam = "Lazio",
                homeScore = 2,
                awayScore = 3,
                date = "22-02-2025",
                league = "Serie A"
            ),
            Match(
                homeTeam = "Schalke",
                awayTeam = "VfB Stuttgart",
                homeScore = 1,
                awayScore = 1,
                date = "23-02-2025",
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Nice",
                awayTeam = "Rennes",
                homeScore = 0,
                awayScore = 1,
                date = "24-02-2025",
                league = "Ligue 1"
            ),
            Match(
                homeTeam = "Everton",
                awayTeam = "Leicester",
                homeScore = 2,
                awayScore = 0,
                date = "25-02-2025",
                league = "Premier League"
            ),
            Match(
                homeTeam = "Celta Vigo",
                awayTeam = "Real Betis",
                homeScore = 1,
                awayScore = 2,
                date = "26-02-2025",
                league = "La Liga"
            ),
            Match(
                homeTeam = "Sampdoria",
                awayTeam = "Torino",
                homeScore = 3,
                awayScore = 1,
                date = "27-02-2025",
                league = "Serie A"
            ),
            Match(
                homeTeam = "Hertha Berlin",
                awayTeam = "Eintracht Frankfurt",
                homeScore = 0,
                awayScore = 2,
                date = "28-02-2025",
                league = "Bundesliga"
            ),
            Match(
                homeTeam = "Lille",
                awayTeam = "Strasbourg",
                homeScore = 1,
                awayScore = 1,
                date = "01-03-2025",
                league = "Ligue 1"
            )
        )
    }


}