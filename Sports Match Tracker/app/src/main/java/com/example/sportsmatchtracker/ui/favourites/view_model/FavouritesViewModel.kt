package com.example.sportsmatchtracker.ui.favourites.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.subscriptions.LeagueSubscription
import com.example.sportsmatchtracker.model.subscriptions.TeamSubscription
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.repository.auth.AuthRepository
import com.example.sportsmatchtracker.repository.matches.MatchesRepository
import com.example.sportsmatchtracker.repository.subscriptions.LeagueSubscriptionsRepository
import com.example.sportsmatchtracker.repository.subscriptions.TeamSubscriptionsRepository
import com.example.sportsmatchtracker.ui.auth.view_model.AuthViewModel
import com.example.sportsmatchtracker.ui.components.TabItem
import com.example.sportsmatchtracker.ui.utils.filterList
import com.example.sportsmatchtracker.ui.utils.liveSearch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouritesViewModel(
    private val teamSubscriptionsRepository: TeamSubscriptionsRepository,
    private val leagueSubscriptionsRepository: LeagueSubscriptionsRepository,
    val matchesRepository: MatchesRepository,
    private val authRepository: AuthRepository
): ViewModel() {
    private val _leaguesSubscriptions = MutableStateFlow<List<LeagueSubscription>>(emptyList())
    val leaguesSubscriptions: StateFlow<List<LeagueSubscription>> = _leaguesSubscriptions.asStateFlow()

    private val _teamsSubscriptions = MutableStateFlow<List<TeamSubscription>>(emptyList())
    val teamsSubscriptions: StateFlow<List<TeamSubscription>> = _teamsSubscriptions.asStateFlow()

    private val _teamMatches = MutableStateFlow<List<Match>>(emptyList())
    val teamMatches: StateFlow<List<Match>> = _teamMatches

    private val _leagueMatches = MutableStateFlow<List<Match>>(emptyList())
    val leagueMatches: StateFlow<List<Match>> = _leagueMatches

    val tabItems: List<TabItem<String?>> = listOf(
        TabItem("teams", "Teams"),
        TabItem("leagues", "Leagues")
    )

    private val _teamSearchResults = MutableStateFlow<List<TeamSubscription>>(emptyList())

    val teamSearchResults: StateFlow<List<TeamSubscription>> = _teamSearchResults.asStateFlow()

    private val _leagueSearchResults = MutableStateFlow<List<LeagueSubscription>>(emptyList())
    val leagueSearchResults: StateFlow<List<LeagueSubscription>> = _leagueSearchResults.asStateFlow()


    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _selectedTab = MutableStateFlow("teams")
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private var searchJob: Job? = null

    // Inject dependencies
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val authRepository = (this[APPLICATION_KEY] as App).authRepository
                val teamSubscriptionsRepository = (this[APPLICATION_KEY] as App).teamsSubscriptionsRepository
                val leagueSubscriptionsRepository = (this[APPLICATION_KEY] as App).leagueSubscriptionsRepository
                val matchesRepository = (this[APPLICATION_KEY] as App).matchesRepository
                FavouritesViewModel(
                    teamSubscriptionsRepository = teamSubscriptionsRepository,
                    leagueSubscriptionsRepository = leagueSubscriptionsRepository,
                    matchesRepository = matchesRepository,
                    authRepository = authRepository
                )
            }
        }
    }


    init {
        viewModelScope.launch {
            leagueSubscriptionsRepository.subscriptionsState.collect { leagueSubscriptions ->
                _leaguesSubscriptions.value = leagueSubscriptions
            }
        }

        viewModelScope.launch {
            teamSubscriptionsRepository.subscriptionsState.collect { teamSubscriptions ->
                _teamsSubscriptions.value = teamSubscriptions
            }
        }
    }

    fun initialize() {
        viewModelScope.launch {
            fetchTeamsSubscriptions()
            fetchLeaguesSubscriptions()
        }
    }

    fun refresh() {
        initialize()
    }

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }
    private fun fetchTeamsSubscriptions() = viewModelScope.launch {
        val userEmail = authRepository.userState.value?.email ?: return@launch
        runCatching {
            teamSubscriptionsRepository.fetchSubscriptions(userEmail)
        }.onFailure { exception ->
            println("Error fetching team subscriptions: ${exception.message}")
        }
    }


    private fun fetchLeaguesSubscriptions() = viewModelScope.launch {
        val userEmail = authRepository.userState.value?.email ?: return@launch
        runCatching {
            leagueSubscriptionsRepository.fetchSubscriptions(userEmail)
        }.onFailure { exception ->
            println("Error fetching league subscriptions: ${exception.message}")
        }
    }

    fun fetchTeamMatches() = viewModelScope.launch {
        val teamMatches = mutableListOf<Match>()
        runCatching {
            for(subscription in _teamsSubscriptions.value) {
                val matches = matchesRepository.fetchTeamMatches(subscription.teamName)
                teamMatches.addAll(matches)
            }
            _teamMatches.value = teamMatches.distinct()
        }.onFailure { exception ->
            println("Error fetching team matches: ${exception.message}")
        }
    }

    fun fetchLeagueMatches() = viewModelScope.launch {
        _leagueMatches.value = emptyList()
        runCatching {
            for (subscription in _leaguesSubscriptions.value) {
                val matches = matchesRepository.fetchLeagueMatches(subscription.leagueName, subscription.leagueCountry)
                _leagueMatches.value += matches
            }
        }.onFailure { exception ->
            println("Error fetching league matches: ${exception.message}")
        }
    }

    suspend fun fetchMatchEvents(match: Match): List<MatchEvent> {
        return runCatching {
            matchesRepository.fetchMatchEvents(match)
        }.onFailure { exception ->
            println("Error fetching match events: ${exception.message}")
        }.getOrElse { emptyList() }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            when (_selectedTab.value) {

                "teams" -> {
                    liveSearch(
                        list = _teamsSubscriptions.value,
                        query = query,
                        selector = { it.teamName },
                        onResult = { _teamSearchResults.value = it }
                    )
                }

                "leagues" -> {
                    liveSearch(
                        list = _leaguesSubscriptions.value,
                        query = query,
                        selector = { it.leagueName },
                        onResult = { _leagueSearchResults.value = it }
                    )
                }
            }
        }
    }

}