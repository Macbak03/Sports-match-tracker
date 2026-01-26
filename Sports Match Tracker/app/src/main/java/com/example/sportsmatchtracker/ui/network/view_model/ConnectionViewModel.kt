package com.example.sportsmatchtracker.ui.network.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.auth.AuthUIState
import com.example.sportsmatchtracker.model.client.Client
import com.example.sportsmatchtracker.repository.client.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ConnectionViewModel(
    private val clientRepository: ClientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(Client())
    val uiState: StateFlow<Client> = _uiState.asStateFlow()

    private val _ipAddress = MutableStateFlow("172.20.10.2")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val clientRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as App).clientRepository
                ConnectionViewModel(
                    clientRepository = clientRepository,
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            clientRepository.clientState.collect {
                _uiState.value = it
            }
        }
        connectToServer()
    }

    fun updateIpAddress(newIp: String) {
        _ipAddress.value = newIp
    }

    fun connectToServer() {
        viewModelScope.launch {
            clientRepository.connectToServer(_ipAddress.value)
        }
    }
}