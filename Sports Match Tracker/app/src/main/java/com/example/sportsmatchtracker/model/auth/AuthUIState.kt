package com.example.sportsmatchtracker.model.auth

data class AuthUIState(
    val email: String = "",
    val password: String = "",
    val repeatPassword: String = "",
    val showEmailError: Boolean = false,
    val showPasswordError: Boolean = false,
    val showRepeatPasswordError: Boolean = false,
    val errorMessage: String = "",
    val emailErrorMessage: String = "",
    val passwordErrorMessage: String = "",
    val repeatPasswordErrorMessage: String = "",
    val isInSignUpState: Boolean = false,
)