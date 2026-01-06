package com.example.sportsmatchtracker.ui.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel

class HomeViewModel(
    private val authRepository: AuthRepository
): ViewModel() {
    // Inject dependencies
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val authRepository = (this[APPLICATION_KEY] as App).authRepository
                HomeViewModel(
                    authRepository = authRepository,
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}