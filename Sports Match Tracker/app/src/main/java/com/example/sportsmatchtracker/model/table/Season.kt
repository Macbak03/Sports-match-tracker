package com.example.sportsmatchtracker.model.table

import java.time.LocalDate

data class Season(
    val dateStart: LocalDate,
    val dateEnd: LocalDate
) {
    override fun toString(): String {
        return "${dateStart.year} - ${dateEnd.year}"
    }
}