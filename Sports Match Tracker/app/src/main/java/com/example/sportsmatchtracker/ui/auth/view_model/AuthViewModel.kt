package com.example.sportsmatchtracker.ui.auth.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.auth.AuthUIState
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel (
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()



    // Inject dependencies
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val authRepository = (this[APPLICATION_KEY] as App).authRepository
                AuthViewModel(
                    authRepository = authRepository,
                )
            }
        }
    }

    init {
        // Observe user state changes
        viewModelScope.launch {
            authRepository.userState.collect { user ->
                _user.value = user
            }
        }

    }

    fun onEmailChange(newValue: String) =
        _uiState.update { it.copy(email = newValue, showEmailError = false) }

    fun onNickChange(newValue: String) =
        _uiState.update { it.copy(nick = newValue) }

    fun onPasswordChange(newValue: String) =
        _uiState.update { it.copy(password = newValue, showPasswordError = false) }

    fun onRepeatPasswordChange(newValue: String) =
        _uiState.update { it.copy(repeatPassword = newValue, showRepeatPasswordError = false) }

    fun setSignUpState(newValue: Boolean) =
        _uiState.update { it.copy(isInSignUpState = newValue) }

    fun login() {
        val currentState = _uiState.value
        
        // Validate inputs
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(showEmailError = true, emailErrorMessage = "Email cannot be empty") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(showPasswordError = true, passwordErrorMessage = "Password cannot be empty") }
            return
        }
        
        viewModelScope.launch {
            val loginSuccessful = authRepository.login(currentState.email, currentState.password)

            if (!loginSuccessful) {
                _uiState.update {
                    it.copy(
                        showPasswordError = true,
                        passwordErrorMessage = "Invalid email or password"
                    )
                }
            }
        }
    }

    fun register() {
        val currentState = _uiState.value

        // Validate inputs
        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(showEmailError = true, emailErrorMessage = "Email cannot be empty") }
            return
        }
        if (currentState.password.isBlank()) {
            _uiState.update { it.copy(showPasswordError = true, passwordErrorMessage = "Password cannot be empty") }
            return
        }
        if (currentState.repeatPassword.isBlank()) {
            _uiState.update { it.copy(showRepeatPasswordError = true, repeatPasswordErrorMessage = "Repeat password cannot be empty") }
            return
        }
        if(currentState.password != currentState.repeatPassword) {
            _uiState.update { it.copy(showRepeatPasswordError = true, repeatPasswordErrorMessage = "Passwords do not match") }
        }

        viewModelScope.launch {
            val registerSuccessful = authRepository.register(currentState.email, currentState.nick, currentState.password)
        }
    }
    
    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(email = "", password = "") }
    }

    private fun updateUIBasedOnErrorType(exception: Throwable) {
        val authError = AuthError(errorMessage = "error")
        if (authError.emailError && authError.passwordError) {
            _uiState.update { it.copy(
                showEmailError = true ,
                showPasswordError = true,
                showRepeatPasswordError = false,
                passwordErrorMessage = authError.errorMessage
            )}
        }
        else if (authError.passwordError) {
            _uiState.update { it.copy(
                showPasswordError = true,
                showEmailError = false,
                showRepeatPasswordError = false,
                passwordErrorMessage = authError.errorMessage
            )}
        }
        else if (authError.emailError) {
            _uiState.update { it.copy(
                showEmailError = true,
                showPasswordError = false,
                showRepeatPasswordError = false,
                emailErrorMessage = authError.errorMessage
            )}
        }
        else if(authError.repeatPasswordError) {
            _uiState.update { it.copy(
                showEmailError = false,
                showPasswordError = false,
                showRepeatPasswordError = true,
                repeatPasswordErrorMessage = authError.errorMessage
            ) }
        }
    }
}