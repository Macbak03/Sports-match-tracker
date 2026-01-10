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
            isInitialized = true
        }
    }

    fun refreshMatches() {
        viewModelScope.launch {
            try {
                matchesRepository.fetchMatches()
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
            tabs.add(TabItem(sport, sport.name))
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

}