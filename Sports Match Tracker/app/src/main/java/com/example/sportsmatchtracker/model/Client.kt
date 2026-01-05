package com.example.sportsmatchtracker.model

data class Client(
    val connected: Boolean = false,
    val isLoading: Boolean = false,
    val connectionStatus: String = "Not connected",
    val responseFromServer: String? = null
)
