package com.example.sportsmatchtracker.ui.settings.view_model


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.settings.NickChangeStatus
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    // Inject dependencies

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _nickStatus = MutableStateFlow<NickChangeStatus?>(null)
    val nickStatus: StateFlow<NickChangeStatus?> = _nickStatus.asStateFlow()


    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val settingsRepository = (this[APPLICATION_KEY] as App).settingsRepository
                val authRepository = (this[APPLICATION_KEY] as App).authRepository
                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    authRepository = authRepository
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            authRepository.userState.collect { user ->
                settingsRepository.setUserState(user)
                _user.value = user
            }
        }
    }

    fun logout() {
        settingsRepository.logout()
        authRepository.logout()
    }

    fun updateNick(newNick: String) = viewModelScope.launch {
        val email = _user.value?.email ?: return@launch
        runCatching {
            settingsRepository.updateUserNick(email, newNick)
            _user.value = _user.value?.copy(nick = newNick)
            _nickStatus.value = NickChangeStatus(message = "Nick updated successfully", error = false)
        }.onFailure { exception ->
            _nickStatus.value = NickChangeStatus(message = exception.message ?: "Unknown error", error = true)
        }
    }
    
    fun clearNickStatus() {
        _nickStatus.value = null
    }
}