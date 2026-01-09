package com.example.sportsmatchtracker.model.league

import com.example.sportsmatchtracker.model.subscriptions.TeamSubscription

data class Team(
    val name: String,
    val city: String,
    val isSubscribed: Boolean = false
) {
    fun toSubscription(): TeamSubscription {
        return TeamSubscription(
            teamName = name
        )
    }
}