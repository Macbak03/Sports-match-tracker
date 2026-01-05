package com.example.sportsmatchtracker.model.auth

data class AuthError(
    val errorMessage: String,
    val emailError: Boolean = false,
    val passwordError: Boolean = false,
    val repeatPasswordError: Boolean = false,
): Exception(errorMessage)
