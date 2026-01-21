package com.example.sportsmatchtracker.repository.matches

import com.example.sportsmatchtracker.model.formatter.CustomDateFormatter
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.match.MatchResult
import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.database.LogicalOperator
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.sport.SportName
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.collections.set

class MatchesRepository : Repository() {
    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matchesState: StateFlow<List<Match>> = _matchesState.asStateFlow()

    private fun buildMatchesRequest(
        where: List<WhereCondition>? = null,
        orderBy: String? = null,
        orderDirection: String? = null,
        limit: Int? = null
    ): JSONObject {
        return selectWithJoinRequest(
            table = DatabaseSchema.Matches.TABLE_NAME,
            columns = listOf(
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.START_DATE} as start_date",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.HOME_SCORE} as home_score",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.AWAY_SCORE} as away_score",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.SEASON_START_DATE} as season_start_date",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.SEASON_END_DATE} as season_end_date",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.BUILDING_NAME} as building_name",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.HOME_TEAM_NAME} as home_team_name",
                "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.AWAY_TEAM_NAME} as away_team_name",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME} as league_name",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.COUNTRY} as league_country",
                "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME} as sport_name"
            ),
            joins = listOf(
                JoinClause(
                    table = DatabaseSchema.Leagues.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.SEASON_LEAGUE_NAME}",
                    onRight = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME}"
                ),
                JoinClause(
                    table = DatabaseSchema.Sports.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.SPORTS_NAME}",
                    onRight = "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME}"
                )
            ),
            where = where,
            orderBy = orderBy,
            orderDirection = orderDirection,
            limit = limit
        )
    }

    private suspend fun parseMatchesResponse(request: JSONObject): List<Match> = withConnectionCheck {
        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")

                val matches = mutableListOf<Match>()

                for (i in 0 until count) {
                    try {
                        val row = data.getJSONObject(i)
                        val localDateTime =
                            LocalDateTime.parse(row.getString("start_date"), CustomDateFormatter.DATE_TIME)
                        val matchDateTime = localDateTime
                            .atZone(ZoneId.of("Europe/Warsaw"))
                            .toInstant()
                        val seasonStartDate =
                            LocalDate.parse(row.getString("season_start_date"), CustomDateFormatter.DATE)
                        val seasonEndDate =
                            LocalDate.parse(row.getString("season_end_date"), CustomDateFormatter.DATE)
                        val sportName = SportName.fromString(row.getString("sport_name"))
                        val league = League(
                            name = row.getString("league_name"),
                            country = row.getString("league_country"),
                            sport = Sport(name = sportName)
                        )
                        val events = emptyList<MatchEvent>()

                        matches.add(
                            Match(
                                homeTeam = row.getString("home_team_name"),
                                awayTeam = row.getString("away_team_name"),
                                homeScore = row.optInt("home_score"),
                                awayScore = row.optInt("away_score"),
                                matchDateTime = matchDateTime,
                                league = league,
                                events = events,
                                matchStadium = row.getString("building_name"),
                                seasonStartDate = seasonStartDate,
                                seasonEndDate = seasonEndDate
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                matches
            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchMatches() {
        val request = buildMatchesRequest()
        try {
            _matchesState.value = parseMatchesResponse(request)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchTeamMatches(teamName: String): List<Match> {
        val request = buildMatchesRequest(
            where = listOf(
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.HOME_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.AWAY_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                )
            )
        )
        try {
            return parseMatchesResponse(request)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchLeagueMatches(leagueName: String, leagueCountry: String): List<Match> {
        val request = buildMatchesRequest(
            where = listOf(
                WhereCondition(
                    column = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME}",
                    operator = "=",
                    value = leagueName
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.COUNTRY}",
                    operator = "=",
                    value = leagueCountry,
                )
            )
        )
        try {
            return parseMatchesResponse(request)
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchMatchEvents(match: Match): List<MatchEvent> = withConnectionCheck {
        val matchStartDate = match.matchDateTime
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(CustomDateFormatter.DATE_TIME)


        val request = selectRequest(
            table = DatabaseSchema.MatchEvents.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.MatchEvents.GAME_TIME,
                DatabaseSchema.MatchEvents.EVENT
            ),
            where = listOf(
                WhereCondition(
                    column = DatabaseSchema.MatchEvents.MATCH_START_DATE,
                    operator = "=",
                    value = matchStartDate
                ),
                WhereCondition(
                    column = DatabaseSchema.MatchEvents.MATCH_SEASON_START_DATE,
                    operator = "=",
                    value = match.seasonStartDate
                ),
                WhereCondition(
                    column = DatabaseSchema.MatchEvents.MATCH_SEASON_END_DATE,
                    operator = "=",
                    value = match.seasonEndDate
                ),
                WhereCondition(
                    column = DatabaseSchema.MatchEvents.MATCH_SEASON_LEAGUE_NAME,
                    operator = "=",
                    value = match.league.name
                ),
                WhereCondition(
                    column = DatabaseSchema.MatchEvents.MATCH_SEASON_LEAGUE_COUNTRY,
                    operator = "=",
                    value = match.league.country
                )
            )
        )

        val response = socketManager.sendRequestWithResponse(request) ?: return@withConnectionCheck emptyList()

        try {
            val jsonResponse = JSONObject(response)
            if (jsonResponse.getString("status") == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")
                val events = mutableListOf<MatchEvent>()

                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    events.add(
                        MatchEvent(
                            gameTime = row.getString(DatabaseSchema.MatchEvents.GAME_TIME),
                            event = row.getString(DatabaseSchema.MatchEvents.EVENT)
                        )
                    )
                }
                events
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getSports(): List<Sport> = withConnectionCheck {
        val request = selectRequest(
            table = DatabaseSchema.Sports.TABLE_NAME,
            columns = listOf(DatabaseSchema.Sports.NAME)
        )
        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")
            val sports = mutableListOf<Sport>()

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")
                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    val sportName = SportName.fromString(row.getString(DatabaseSchema.Sports.NAME))
                    sports.add(
                        Sport(name = sportName)
                    )
                }
                sports
            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchLastFiveMatchesResults(teamName: String): List<MatchResult> {
        val currentDate = LocalDateTime.now()
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(CustomDateFormatter.DATE_TIME)

        val request = buildMatchesRequest(
            where = listOf(
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.START_DATE}",
                    operator = "<",
                    value = currentDate,
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.HOME_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.AWAY_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                )
            ),
            orderBy = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.START_DATE}",
            orderDirection = "DESC",
            limit = 5
        )

        try {
            val results = mutableListOf<MatchResult>()
            for (match in parseMatchesResponse(request)) {
                val result = match.getResult(teamName)
                if (result != null) {
                    results.add(result)
                }
            }
            return results
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchNextMatch(teamName: String): Match? {
        val currentDate = LocalDateTime.now()
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(CustomDateFormatter.DATE_TIME)

        val request = buildMatchesRequest(
            where = listOf(
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.START_DATE}",
                    operator = ">=",
                    value = currentDate,
                    logicalOperator = LogicalOperator.AND
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.HOME_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                ),
                WhereCondition(
                    column = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.AWAY_TEAM_NAME}",
                    operator = "=",
                    value = teamName,
                    logicalOperator = LogicalOperator.OR,
                    group = "team"
                )
            ),
            orderBy = "${DatabaseSchema.Matches.TABLE_NAME}.${DatabaseSchema.Matches.START_DATE}",
            orderDirection = "ASC",
            limit = 1
        )
        try {
            val matches = parseMatchesResponse(request)
            return matches.firstOrNull()
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun fetchBuildings(): List<String> = withConnectionCheck {
        val request = selectRequest(
            table = DatabaseSchema.Buildings.TABLE_NAME,
            columns = listOf(DatabaseSchema.Buildings.NAME)
        )
        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        
        val buildings = mutableListOf<String>()
        val jsonResponse = JSONObject(response)
        if (jsonResponse.getString("status") == "success") {
            val data = jsonResponse.getJSONArray("data")
            for (i in 0 until jsonResponse.getInt("count")) {
                buildings.add(data.getJSONObject(i).getString(DatabaseSchema.Buildings.NAME))
            }
        }
        buildings
    }

    suspend fun insertMatch(match: Match) = withConnectionCheck {
        val startDateFormatted = match.matchDateTime.atZone(ZoneId.of("Europe/Warsaw")).format(CustomDateFormatter.DATE_TIME)
        val seasonStartFormatted = match.seasonStartDate.format(CustomDateFormatter.DATE)
        val seasonEndFormatted = match.seasonEndDate.format(CustomDateFormatter.DATE)

        val request = insertRequest(
            table = DatabaseSchema.Matches.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.Matches.START_DATE,
                DatabaseSchema.Matches.HOME_SCORE,
                DatabaseSchema.Matches.AWAY_SCORE,
                DatabaseSchema.Matches.SEASON_START_DATE,
                DatabaseSchema.Matches.SEASON_END_DATE,
                DatabaseSchema.Matches.SEASON_LEAGUE_NAME,
                DatabaseSchema.Matches.SEASON_LEAGUE_COUNTRY,
                DatabaseSchema.Matches.BUILDING_NAME,
                DatabaseSchema.Matches.HOME_TEAM_NAME,
                DatabaseSchema.Matches.AWAY_TEAM_NAME
            ),
            values = listOf(
                startDateFormatted,
                match.homeScore,
                match.awayScore,
                seasonStartFormatted,
                seasonEndFormatted,
                match.league.name,
                match.league.country,
                match.matchStadium,
                match.homeTeam,
                match.awayTeam
            )
        )

        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")


        val jsonResponse = JSONObject(response)
        if (jsonResponse.getString("status") != "success") {
            throw Exception(jsonResponse.optString("message"))
        }
        
        // Refresh matches after insertion
        // Ideally we should call initialize() or refresh() but that belongs to ViewModel.
        // We can just emit new state if we want, but here we just return success.
    }

}