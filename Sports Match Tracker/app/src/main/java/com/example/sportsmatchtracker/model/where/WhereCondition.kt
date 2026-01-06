package com.example.sportsmatchtracker.model.where

data class WhereCondition(
    val column: String,
    val operator: String,
    val value: Any
)
