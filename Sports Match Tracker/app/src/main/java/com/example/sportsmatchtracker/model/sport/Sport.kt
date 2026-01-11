package com.example.sportsmatchtracker.model.sport

data class Sport(
    val name: SportName
)

enum class SportName(val label: String) {
    FOOTBALL("Football"),
    BASKETBALL("Basketball"),
    VOLLEYBALL("Volleyball"),
    UNKNOWN("Unknown");


    companion object {
        fun fromString(value: String): SportName {
            return when (value.lowercase()) {
                "football" -> FOOTBALL
                "basketball" -> BASKETBALL
                "volleyball" -> VOLLEYBALL
                else -> UNKNOWN
            }
        }
    }
}