package com.example.sportsmatchtracker.model.team

import com.example.sportsmatchtracker.model.subscriptions.TeamSubscription
import java.time.LocalDate

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