package com.example.sportsmatchtracker.model.database

data class JoinClause(
    val table: String,
    val onLeft: String,
    val onRight: String,
    val type: String = "INNER" // INNER, LEFT, RIGHT
)
