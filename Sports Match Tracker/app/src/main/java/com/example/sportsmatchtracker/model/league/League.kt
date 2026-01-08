package com.example.sportsmatchtracker.model.league

import com.example.sportsmatchtracker.model.sport.Sport

data class League(
    val name: String,
    val country: String,
    val sport: Sport
)
