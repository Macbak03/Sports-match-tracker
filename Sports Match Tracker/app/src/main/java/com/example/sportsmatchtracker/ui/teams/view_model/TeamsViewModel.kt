package com.example.sportsmatchtracker.ui.teams.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.repository.teams.TeamsRepository
import com.example.sportsmatchtracker.ui.home.view_model.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val teamsRepository: TeamsRepository
): ViewModel() {
    private val _teams = MutableStateFlow<List<Team>>(emptyList())
    val teams: StateFlow<List<Team>> = _teams.asStateFlow()
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
        // Observe user state changes
        viewModelScope.launch {
            teamsRepository.teamsState.collect { teams ->
                _teams.value = teams
            }
        }

    }

    fun getTeams() {
        viewModelScope.launch {
            teamsRepository.getTeams()
        }
    }
}