package com.example.sportsmatchtracker.ui.teams.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.subscriptions.LeagueSubscription
import com.example.sportsmatchtracker.model.subscriptions.TeamSubscription
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.repository.subscriptions.LeagueSubscriptionsRepository
import com.example.sportsmatchtracker.repository.subscriptions.TeamSubscriptionsRepository
import com.example.sportsmatchtracker.ui.components.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val leaguesRepository: LeaguesRepository,
    private val teamSubscriptionsRepository: TeamSubscriptionsRepository,
    private val leagueSubscriptionsRepository: LeagueSubscriptionsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _leagues = MutableStateFlow<List<League>>(emptyList())
    val leagues: StateFlow<List<League>> = _leagues.asStateFlow()

    private val _sports = MutableStateFlow<List<Sport>>(emptyList())
    val sports: StateFlow<List<Sport>> = _sports.asStateFlow()

    private val _tabItems = MutableStateFlow<List<TabItem<Sport?>>>(emptyList())
    val tabItems: StateFlow<List<TabItem<Sport?>>> = _tabItems.asStateFlow()

    private var isInitialized = false

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val teamsRepository = (this[APPLICATION_KEY] as App).leaguesRepository
                val teamSubscriptionsRepository =
                    (this[APPLICATION_KEY] as App).teamsSubscriptionsRepository
                val leagueSubscriptionsRepository =
                    (this[APPLICATION_KEY] as App).leagueSubscriptionsRepository
                val authRepository = (this[APPLICATION_KEY] as App).authRepository
                TeamsViewModel(
                    leaguesRepository = teamsRepository,
                    teamSubscriptionsRepository = teamSubscriptionsRepository,
                    leagueSubscriptionsRepository = leagueSubscriptionsRepository,
                    authRepository = authRepository
                )
            }
        }
    }

    init {
        viewModelScope.launch {
            leaguesRepository.leaguesState.collect { rawLeagues ->
                updateLeaguesWithSubscriptions(rawLeagues)
            }
        }

        viewModelScope.launch {
            leagueSubscriptionsRepository.subscriptionsState.collect {
                updateLeaguesWithSubscriptions(leaguesRepository.leaguesState.value)
            }
        }

        viewModelScope.launch {
            teamSubscriptionsRepository.subscriptionsState.collect {
                updateLeaguesWithSubscriptions(leaguesRepository.leaguesState.value)
            }
        }
    }

    fun initialize() {
        if (isInitialized) return

        viewModelScope.launch {
            fetchSports()
            setTabItems()
            fetchSubscriptions()
            fetchLeagues()
            isInitialized = true
        }
    }

    private fun updateLeaguesWithSubscriptions(rawLeagues: List<League>) {
        val leagueSubscriptions = leagueSubscriptionsRepository.subscriptionsState.value
        val teamSubscriptions = teamSubscriptionsRepository.subscriptionsState.value

        val updatedLeagues = rawLeagues.map { league ->
            // Check if league is subscribed
            val isLeagueSubscribed = leagueSubscriptions.any {
                it.leagueName == league.name && it.leagueCountry == league.country
            }

            // Update teams with subscription status
            val updatedTeams = league.teams.map { team ->
                val isTeamSubscribed = teamSubscriptions.any {
                    it.teamName == team.name
                }
                team.copy(isSubscribed = isTeamSubscribed)
            }

            league.copy(
                isSubscribed = isLeagueSubscribed,
                teams = updatedTeams
            )
        }

        _leagues.value = updatedLeagues
    }

    private suspend fun fetchSubscriptions() {
        val userEmail = authRepository.userState.value?.email ?: return

        runCatching {
            leagueSubscriptionsRepository.fetchSubscriptions(userEmail)
            teamSubscriptionsRepository.fetchSubscriptions(userEmail)
        }.onFailure { exception ->
            println("Error fetching subscriptions: ${exception.message}")
        }
    }

    private suspend fun fetchLeagues() {
        runCatching {
            leaguesRepository.fetchLeagues()
        }.onFailure { exception ->
            println("Error fetching teams: ${exception.message}")
        }
    }

    private suspend fun fetchSports() {
        runCatching {
            _sports.value = leaguesRepository.getSports()
        }.onFailure { exception ->
            println("Error fetching sports: ${exception.message}")
        }
    }

    fun subscribeTeam(userEmail: String, subscription: TeamSubscription) = viewModelScope.launch {
        runCatching {
            teamSubscriptionsRepository.addSubscription(userEmail, subscription)
        }.onFailure { exception ->
            println("Error subscribing to team: ${exception.message}")
        }
    }

    fun unsubscribeTeam(userEmail: String, subscription: TeamSubscription) = viewModelScope.launch {
        runCatching {
            teamSubscriptionsRepository.unsubscribe(userEmail, subscription)
        }.onFailure { exception ->
            println("Error unsubscribing from team: ${exception.message}")
        }
    }

    fun subscribeLeague(userEmail: String, subscription: LeagueSubscription) =
        viewModelScope.launch {
            runCatching {
                leagueSubscriptionsRepository.addSubscription(userEmail, subscription)
            }.onFailure { exception ->
                println("Error subscribing to league: ${exception.message}")
            }
        }

    fun unsubscribeLeague(userEmail: String, subscription: LeagueSubscription) = viewModelScope.launch {
        runCatching {
            leagueSubscriptionsRepository.unsubscribe(userEmail, subscription)
        }.onFailure { exception ->
            println("Error unsubscribing from league: ${exception.message}")
        }
    }

    private fun setTabItems() {
        val tabs = mutableListOf<TabItem<Sport?>>()
        tabs.add(TabItem(null, "All"))
        _sports.value.forEach { sport ->
            tabs.add(TabItem(sport, sport.name))
        }
        _tabItems.value = tabs
    }
}