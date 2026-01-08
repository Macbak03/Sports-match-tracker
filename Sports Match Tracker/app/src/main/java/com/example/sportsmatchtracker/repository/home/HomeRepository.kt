package com.example.sportsmatchtracker.repository.home

import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.match.Match
import com.example.sportsmatchtracker.model.match.MatchEvent
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class HomeRepository : Repository() {
    private val _matchesState = MutableStateFlow<List<Match>>(emptyList())
    val matchesState: StateFlow<List<Match>> = _matchesState.asStateFlow()

    suspend fun fetchMatches() {
        val request = selectWithJoinRequest(
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
            )
        )

        val response = socketManager.sendRequestWithResponse(request) ?: throw Exception("No response from server")

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")

                val matches = mutableListOf<Match>()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                for (i in 0 until count) {
                    try {
                        val row = data.getJSONObject(i)
                        val localDateTime = LocalDateTime.parse(row.getString("start_date"), formatter)
                        val matchDateTime = localDateTime
                            .atZone(ZoneId.of("Europe/Warsaw"))
                            .toInstant()
                        val league = League(
                            name = row.getString("league_name"),
                            country = row.getString("league_country"),
                            sport = Sport(name = row.getString("sport_name"))
                        )
                        val events = emptyList<MatchEvent>()

                        matches.add(Match(
                            homeTeam = row.getString("home_team_name"),
                            awayTeam = row.getString("away_team_name"),
                            homeScore = row.optInt("home_score"),
                            awayScore = row.optInt("away_score"),
                            matchDateTime = matchDateTime,
                            league = league,
                            events = events,
                            matchStadium = row.getString("building_name"),
                            seasonStartDate = row.getString("season_start_date"),
                            seasonEndDate = row.getString("season_end_date")
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _matchesState.value = matches

            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun fetchMatchEvents(match: Match): List<MatchEvent> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val matchStartDate = match.matchDateTime
            .atZone(ZoneId.of("Europe/Warsaw"))
            .format(formatter)

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

        val response = socketManager.sendRequestWithResponse(request) ?: return emptyList()

        return try {
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

    suspend fun getSports() : List<Sport> {
        val request = selectRequest(
            table = DatabaseSchema.Sports.TABLE_NAME,
            columns = listOf(DatabaseSchema.Sports.NAME)
        )
        val response = socketManager.sendRequestWithResponse(request) ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")
            val sports = mutableListOf<Sport>()

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")
                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    sports.add(
                        Sport(name = row.getString(DatabaseSchema.Sports.NAME))
                    )
                }
                return sports
            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

}