package com.example.sportsmatchtracker.ui.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.ConnectStatus
import com.example.sportsmatchtracker.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ConnectStatus())
    val uiState: StateFlow<ConnectStatus> = _uiState.asStateFlow()

    init {
        // Obserwuj zmiany statusu połączenia
        viewModelScope.launch {
            clientRepository.connected.collect { isConnected ->
                _uiState.update { currentState ->
                    currentState.copy(
                        connected = isConnected,
                        connectionStatus = if(isConnected) "Connected to server" else "Not connected"
                    )
                }
            }
        }
    }

    fun connectToServer() {
        clientRepository.connectToServer()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val clientRepository = (this[APPLICATION_KEY] as App).clientRepository
                HomeViewModel(
                    clientRepository = clientRepository,
                )
            }
        }
    }
}