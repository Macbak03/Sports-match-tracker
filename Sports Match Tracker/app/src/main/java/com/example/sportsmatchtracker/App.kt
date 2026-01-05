package com.example.sportsmatchtracker

import android.app.Application
import com.example.sportsmatchtracker.repository.ClientRepository

class App: Application() {
    val clientRepository: ClientRepository by lazy {
        ClientRepository()
    }
}