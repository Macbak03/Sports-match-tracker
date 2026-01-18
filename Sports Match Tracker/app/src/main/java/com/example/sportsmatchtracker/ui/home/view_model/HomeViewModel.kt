package com.example.sportsmatchtracker.ui.home.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.ui.components.TabItem
import com.example.sportsmatchtracker.repository.matches.MatchesRepository
import com.example.sportsmatchtracker.ui.utils.filterList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val matchesRepository: MatchesRepository
): ViewModel() {

    val matches: StateFlow<List<Match>> = matchesRepository.matchesState

    private val _sports = MutableStateFlow<List<Sport>>(emptyList())
    val sports: StateFlow<List<Sport>> = _sports.asStateFlow()

    private val _tabItems = MutableStateFlow<List<TabItem<Sport?>>>(emptyList())
    val tabItems: StateFlow<List<TabItem<Sport?>>> = _tabItems.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Match>>(emptyList())
    val searchResults: StateFlow<List<Match>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: kotlinx.coroutines.Job? = null
    private var isInitialized = false

    init {
        // debug: log matches and tabItems updates
        viewModelScope.launch {
            matchesRepository.matchesState.collect { list ->
                println("HomeViewModel: matches updated, size=${list.size}")
            }
        }

        viewModelScope.launch {
            _tabItems.collect { tabs ->
                println("HomeViewModel: tabItems updated, size=${tabs.size}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val homeRepository = (this[APPLICATION_KEY] as App).matchesRepository
                HomeViewModel(
                    matchesRepository = homeRepository
                )
            }
        }
    }
    fun initialize() {
        if (isInitialized) return

        viewModelScope.launch {
            fetchSports()
            setTabItems()
            refreshMatches()
            _searchResults.value = matches.value
            isInitialized = true

        }
    }

    fun refreshMatches() {
        viewModelScope.launch {
            try {
                matchesRepository.fetchMatches()
                _searchResults.value = matches.value
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private suspend fun fetchSports() {
        runCatching {
            _sports.value = matchesRepository.getSports()
        }.onFailure { exception ->
            println("Error fetching sports: ${exception.message}")
        }
    }
    private fun setTabItems() {
        val tabs = mutableListOf<TabItem<Sport?>>()
        tabs.add(TabItem(null, "All"))
        _sports.value.forEach { sport ->
            tabs.add(TabItem(sport, sport.name.label))
        }
        _tabItems.value = tabs
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
            _isSearching.value = true
            try {
                delay(300) // debounce
                val allMatches = matches.value
                _searchResults.value = filterList(allMatches, query) { match ->
                    "${match.homeTeam} ${match.awayTeam}"
                }
            } catch (e: Exception) {
                println("Search error: ${e.message}")
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = matches.value
        _isSearching.value = false
    }

}