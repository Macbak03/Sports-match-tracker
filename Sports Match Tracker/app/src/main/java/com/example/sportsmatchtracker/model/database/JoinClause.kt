package com.example.sportsmatchtracker.model.database

data class JoinClause(
    val table: String,
    val onLeft: String,
    val onRight: String,
    val type: JoinType = JoinType.INNER
)

enum class JoinType(val type : String){
    INNER("INNER"),
    LEFT("LEFT"),
    RIGHT("RIGHT")
}