package com.example.sportsmatchtracker.model.league

import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.subscriptions.LeagueSubscription
import com.example.sportsmatchtracker.model.team.Team

data class League(
    val name: String,
    val country: String,
    val sport: Sport,
    val teams: List<Team> = emptyList(),
    val isSubscribed: Boolean = false
) {
    fun toSubscription(): LeagueSubscription {
        return LeagueSubscription(
            leagueName = name,
            leagueCountry = country
        )
    }
}
