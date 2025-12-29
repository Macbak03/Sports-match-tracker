package com.example.sportsmatchtracker.model

data class ConnectStatus(
    val connected: Boolean = false,
    val connectionStatus: String = "Not connected"
)
