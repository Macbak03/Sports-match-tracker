package com.example.sportsmatchtracker.ui.main.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.repository.client.ClientRepository
import kotlinx.coroutines.launch

class MainViewModel(
    val clientRepository: ClientRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val clientRepository =
                    (this[ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY] as App).clientRepository
                MainViewModel(
                    clientRepository = clientRepository,
                )
            }
        }
    }

    init {
        // Auto-connect on init
        connectToServer()
    }

    fun connectToServer() {
        viewModelScope.launch {
            clientRepository.connectToServer()
        }
    }
}