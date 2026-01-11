package com.example.sportsmatchtracker.model.formatter

import java.time.format.DateTimeFormatter

object CustomDateFormatter {
    val DATE_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}