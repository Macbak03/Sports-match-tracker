package com.example.sportsmatchtracker.ui.tables.view_model

import androidx.compose.material.icons.materialIcon
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.sportsmatchtracker.App
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchResult
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.sport.SportName
import com.example.sportsmatchtracker.model.table.Season
import com.example.sportsmatchtracker.model.table.TableStanding
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.repository.matches.MatchesRepository
import com.example.sportsmatchtracker.repository.seasons.SeasonsRepository
import com.example.sportsmatchtracker.repository.tables.TablesRepository
import com.example.sportsmatchtracker.ui.components.TabItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TablesViewModel(
    private val leaguesRepository: LeaguesRepository,
    private val seasonsRepository: SeasonsRepository,
    private val tablesRepository: TablesRepository,
    private val matchesRepository: MatchesRepository
): ViewModel() {
    private val _seasons = MutableStateFlow<List<Season>>(emptyList())
    val seasons: StateFlow<List<Season>> = _seasons.asStateFlow()

    private val _table = MutableStateFlow<List<TableStanding>>(emptyList())
    val table: StateFlow<List<TableStanding>> = _table.asStateFlow()


    private val _sports = MutableStateFlow<List<Sport>>(emptyList())

    private val _sportTabItems = MutableStateFlow<List<TabItem<Sport>>>(emptyList())
    val sportTabItems: StateFlow<List<TabItem<Sport>>> = _sportTabItems.asStateFlow()

    private val _leagueTabItems = MutableStateFlow<List<TabItem<League>>>(emptyList())
    val leagueTabItems: StateFlow<List<TabItem<League>>> = _leagueTabItems.asStateFlow()

    private var isInitialized = false

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val leaguesRepository = (this[APPLICATION_KEY] as App).leaguesRepository
                val seasonsRepository = (this[APPLICATION_KEY] as App).seasonsRepository
                val tablesRepository = (this[APPLICATION_KEY] as App).tablesRepository
                val matchesRepository = (this[APPLICATION_KEY] as App).matchesRepository
                TablesViewModel(
                    leaguesRepository = leaguesRepository,
                    seasonsRepository = seasonsRepository,
                    tablesRepository = tablesRepository,
                    matchesRepository = matchesRepository
                )
            }
        }
    }

    fun initialize() {
        if (isInitialized) return

        viewModelScope.launch {
            fetchSports()
            setSportTabItems()
            setLeagueTabItems(SportName.FOOTBALL.label)
            isInitialized = true
        }
    }

    fun refresh() {
        isInitialized = false
        initialize()
    }

    suspend fun fetchSeasonsForLeague(league: League) {
        runCatching {
            _seasons.value = seasonsRepository.fetchSeasonsForLeague(league)
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

    private fun setSportTabItems() {
        val tabs = mutableListOf<TabItem<Sport>>()
        _sports.value.forEach { sport ->
            tabs.add(TabItem(sport, sport.name.label))
        }
        _sportTabItems.value = tabs
    }

    suspend fun setLeagueTabItems(sportName: String) {
        runCatching {
            val tabs = mutableListOf<TabItem<League>>()
            val leagues = leaguesRepository.fetchLeaguesForSport(sportName)
            leagues.forEach { league ->
                tabs.add(TabItem(league, league.name))
            }
            _leagueTabItems.value = tabs
        }.onFailure { exception ->
            println("Error fetching leagues: ${exception.message}")
        }

    }

    suspend fun fetchTable(season: Season, league: League) {
        runCatching {
            _table.value = tablesRepository.fetchSeasonTable(season, league)

        }.onFailure { exception->
            println("Error fetching table: ${exception.message}")
        }
    }

    suspend fun getNextTeamMatch(teamName: String): Match? {
        runCatching {
            return matchesRepository.fetchNextMatch(teamName)
        }.onFailure { exception ->
            println("Error fetching next match: ${exception.message}")
        }
        return null
    }

    suspend fun getTeamLastMatchesResults(teamName: String): List<MatchResult> {
        runCatching {
            return matchesRepository.fetchLastFiveMatchesResults(teamName)
        }.onFailure { exception ->
            println("Error fetching last match results: ${exception.message}")
        }
        return emptyList()
    }
}