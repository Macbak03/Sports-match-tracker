package com.example.sportsmatchtracker.model.table

import com.example.sportsmatchtracker.model.team.Team

data class TableStanding(
    val team: Team,
    val draws: Int,
    val losses: Int,
    val wins: Int,
    val matchesPlayed: Int,
    val points: Int,
)
