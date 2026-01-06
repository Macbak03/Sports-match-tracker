package com.example.sportsmatchtracker.repository.client

import com.example.sportsmatchtracker.model.client.Client
import com.example.sportsmatchtracker.network.SocketManager
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ClientRepository : Repository(){
    private val _clientState = MutableStateFlow(Client())
    val clientState: StateFlow<Client> = _clientState.asStateFlow()
    suspend fun connectToServer(): Boolean {
        _clientState.update { it.copy(isLoading = true, connectionStatus = "Connecting...") }
        
        val connected = socketManager.connect()
        
        _clientState.update {
            it.copy(
                connected = connected,
                isLoading = false,
                connectionStatus = if (connected) "Connected" else "Failed to connect to server"
            )
        }
        
        return connected
    }

    fun disconnect() {
        socketManager.disconnect()
        _clientState.update {
            it.copy(
                connected = false,
                connectionStatus = "Disconnected"
            )
        }
    }
}