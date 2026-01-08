package com.example.sportsmatchtracker.ui.teams.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.repository.teams.TeamsRepository
import com.example.sportsmatchtracker.ui.components.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val teamsRepository: TeamsRepository
): ViewModel() {
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
    
    private val _sports = MutableStateFlow<List<Sport>>(emptyList())
    val sports: StateFlow<List<Sport>> = _sports.asStateFlow()
    
    private val _tabItems = MutableStateFlow<List<TabItem<Sport?>>>(emptyList())
    val tabItems: StateFlow<List<TabItem<Sport?>>> = _tabItems.asStateFlow()

    private var isInitialized = false

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val teamsRepository = (this[APPLICATION_KEY] as App).teamsRepository
                TeamsViewModel(
                    teamsRepository = teamsRepository
                )
            }
        }
    }
    
    init {
        // Obserwuj zmiany w teamsRepository
        viewModelScope.launch {
            teamsRepository.teamsState.collect { teams ->
                _teams.value = teams
            }
        }
    }

    // Wywołaj tę funkcję przy wejściu na TeamsScreen
    fun initialize() {
        if (isInitialized) return
        
        viewModelScope.launch {
            fetchSports()
            setTabItems()
            fetchTeams()
            isInitialized = true
        }
    }

    private suspend fun fetchTeams() {
        runCatching {
            teamsRepository.fetchTeams()
        }.onFailure { exception ->
            println("Error fetching teams: ${exception.message}")
        }
    }

    private suspend fun fetchSports() {
        runCatching {
            _sports.value = teamsRepository.getSports()
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
}