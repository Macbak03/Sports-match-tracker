package com.example.sportsmatchtracker.ui.auth.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.auth.AuthUIState
import com.example.sportsmatchtracker.repository.ClientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel (
    private val clientRepository: ClientRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()


    // Inject dependencies
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val clientRepository = (this[APPLICATION_KEY] as App).clientRepository
                AuthViewModel(
                    clientRepository = clientRepository,
                )
            }
        }
    }

    init {
        // Observe client state changes
//        viewModelScope.launch {
//            clientRepository.clientState.collect { clientState ->
//                _uiState.value = clientState
//            }
//        }

    }

    fun onEmailChange(newValue: String) =
        _uiState.update { it.copy(email = newValue, showEmailError = false) }

    fun onPasswordChange(newValue: String) =
        _uiState.update { it.copy(password = newValue, showPasswordError = false) }

    fun onRepeatPasswordChange(newValue: String) =
        _uiState.update { it.copy(repeatPassword = newValue, showRepeatPasswordError = false) }

    fun setSignUpState(newValue: Boolean) =
        _uiState.update { it.copy(isInSignUpState = newValue) }

//    fun connectToServer() {
//        clientRepository.connectToServer()
//    }

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