package com.example.sportsmatchtracker.repository.Home

import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeRepository: Repository() {
    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matchesState: StateFlow<List<Match>> = _matchesState.asStateFlow()

    init {
        _matchesState.value = listOf(
            Match( "Real Madrid", "Barcelona", 2, 1, "10-02-2025", "La Liga"),
            Match( "Manchester United", "Liverpool", 1, 3, "11-02-2025", "Premier League"),
            Match( "Bayern Munich", "Borussia Dortmund", 4, 2, "12-02-2025", "Bundesliga"),
            Match( "Juventus", "AC Milan", 2, 2, "13-02-2025", "Serie A"),
            Match( "PSG", "Lyon", 3, 0, "14-02-2025", "Ligue 1"),
            Match( "Chelsea", "Arsenal", 1, 1, "15-02-2025", "Premier League"),
            Match( "Atletico Madrid", "Sevilla", 2, 0, "16-02-2025", "La Liga"),
            Match( "Napoli", "Inter", 0, 2, "17-02-2025", "Serie A"),
            Match( "RB Leipzig", "Bayer Leverkusen", 3, 1, "18-02-2025", "Bundesliga"),
            Match( "Marseille", "Monaco", 2, 2, "19-02-2025", "Ligue 1"),
            Match( "Tottenham", "Manchester City", 1, 4, "20-02-2025", "Premier League"),
            Match( "Valencia", "Real Sociedad", 3, 2, "21-02-2025", "La Liga"),
            Match( "Fiorentina", "Lazio", 2, 3, "22-02-2025", "Serie A"),
            Match( "Schalke", "VfB Stuttgart", 1, 1, "23-02-2025", "Bundesliga"),
            Match("Nice", "Rennes", 0, 1, "24-02-2025", "Ligue 1"),
            Match( "Everton", "Leicester", 2, 0, "25-02-2025", "Premier League"),
            Match( "Celta Vigo", "Real Betis", 1, 2, "26-02-2025", "La Liga"),
            Match( "Sampdoria", "Torino", 3, 1, "27-02-2025", "Serie A"),
            Match( "Hertha Berlin", "Eintracht Frankfurt", 0, 2, "28-02-2025", "Bundesliga"),
            Match( "Lille", "Strasbourg", 1, 1, "01-03-2025", "Ligue 1")
        )
    }


}