package com.example.sportsmatchtracker

import android.app.Application
import com.example.sportsmatchtracker.repository.matches.MatchesRepository
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.client.ClientRepository
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.repository.seasons.SeasonsRepository
import com.example.sportsmatchtracker.repository.subscriptions.LeagueSubscriptionsRepository
import com.example.sportsmatchtracker.repository.subscriptions.TeamSubscriptionsRepository
import com.example.sportsmatchtracker.repository.tables.TablesRepository

class App: Application() {
    val clientRepository: ClientRepository by lazy {
        ClientRepository()
    }
    val authRepository: AuthRepository by lazy {
        AuthRepository()
    }
    val matchesRepository: MatchesRepository by lazy {
        MatchesRepository()
    }
    val leaguesRepository: LeaguesRepository by lazy {
        LeaguesRepository()
    }

    val leagueSubscriptionsRepository: LeagueSubscriptionsRepository by lazy {
        LeagueSubscriptionsRepository()
    }

    val teamsSubscriptionsRepository: TeamSubscriptionsRepository by lazy {
        TeamSubscriptionsRepository()
    }

    val seasonsRepository: SeasonsRepository by lazy {
        SeasonsRepository()
    }

    val tablesRepository: TablesRepository by lazy {
        TablesRepository()
    }
}