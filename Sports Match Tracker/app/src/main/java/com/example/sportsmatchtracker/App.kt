package com.example.sportsmatchtracker

import android.app.Application
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.client.ClientRepository

class App: Application() {
    val clientRepository: ClientRepository by lazy {
        ClientRepository()
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }
}