package com.example.sportsmatchtracker.ui.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.Client
import com.example.sportsmatchtracker.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(Client())
    val uiState: StateFlow<Client> = _uiState.asStateFlow()

    init {
        // Observe client state changes
        viewModelScope.launch {
            clientRepository.clientState.collect { clientState ->
                _uiState.value = clientState
            }
        }
    }

    fun connectToServer() {
        clientRepository.connectToServer()
    }

    // Inject dependencies
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