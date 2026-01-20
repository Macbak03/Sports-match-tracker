package com.example.sportsmatchtracker.repository.leagues

import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.model.sport.SportName
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class LeaguesRepository : Repository() {
    private val _leaguesState = MutableStateFlow<List<League>>(listOf())
    val leaguesState: StateFlow<List<League>> = _leaguesState.asStateFlow()

    suspend fun fetchLeagues() = withConnectionCheck {
        val request = selectWithJoinRequest(
            table = DatabaseSchema.Leagues.TABLE_NAME,
            columns = listOf(
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME} as league_name",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.COUNTRY} as league_country",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.NAME} as team_name",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.CITY} as team_city",
                "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME} as sport_name"
            ),
            joins = listOf(
                JoinClause(
                    table = DatabaseSchema.Teams.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME}",
                    onRight = "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.LEAGUE_NAME}"
                ),
                JoinClause(
                    table = DatabaseSchema.Sports.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.SPORTS_NAME}",
                    onRight = "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME}"
                )
            )
        )

        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")

                // Group data by leagues
                val leaguesMap = mutableMapOf<Pair<String, String>, MutableList<Team>>()
                val sportsMap = mutableMapOf<Pair<String, String>, Sport>()

                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    val leagueName = row.getString("league_name")
                    val leagueCountry = row.getString("league_country")
                    val leagueKey = Pair(leagueName, leagueCountry)

                    // Add sport name for leagues
                    if (!sportsMap.containsKey(leagueKey)) {
                        val sportName = SportName.fromString(row.getString("sport_name"))
                        sportsMap[leagueKey] = Sport(name = sportName)
                    }

                    // add team to the league team list
                    val team = Team(
                        name = row.getString("team_name"),
                        city = row.getString("team_city")
                    )

                    leaguesMap.getOrPut(leagueKey) { mutableListOf() }.add(team)
                }

                val leagues = leaguesMap.map { (key, teams) ->
                    League(
                        name = key.first,
                        country = key.second,
                        sport = sportsMap[key]!!,
                        teams = teams
                    )
                }

                _leaguesState.value = leagues

            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
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

    suspend fun fetchLeaguesForSport(sportName: String): List<League> = withConnectionCheck {
        val request = selectWithJoinRequest(
            table = DatabaseSchema.Leagues.TABLE_NAME,
            columns = listOf(
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME} as league_name",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.COUNTRY} as league_country",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.NAME} as team_name",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.CITY} as team_city",
                "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME} as sport_name"
            ),
            joins = listOf(
                JoinClause(
                    table = DatabaseSchema.Teams.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME}",
                    onRight = "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.LEAGUE_NAME}"
                ),
                JoinClause(
                    table = DatabaseSchema.Sports.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.SPORTS_NAME}",
                    onRight = "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME}"
                )
            ),
            where = listOf(
                WhereCondition(
                    column = "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME}",
                    operator = "=",
                    value = sportName
                )
            )
        )

        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")

                // Group data by leagues
                val leaguesMap = mutableMapOf<Pair<String, String>, MutableList<Team>>()
                val sportsMap = mutableMapOf<Pair<String, String>, Sport>()

                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    val leagueName = row.getString("league_name")
                    val leagueCountry = row.getString("league_country")
                    val leagueKey = Pair(leagueName, leagueCountry)

                    // Add sport name for leagues
                    if (!sportsMap.containsKey(leagueKey)) {
                        val sportName = SportName.fromString(row.getString("sport_name"))
                        sportsMap[leagueKey] = Sport(name = sportName)
                    }

                    // add team to the league team list
                    val team = Team(
                        name = row.getString("team_name"),
                        city = row.getString("team_city")
                    )

                    leaguesMap.getOrPut(leagueKey) { mutableListOf() }.add(team)
                }

                val leagues = leaguesMap.map { (key, teams) ->
                    League(
                        name = key.first,
                        country = key.second,
                        sport = sportsMap[key]!!,
                        teams = teams
                    )
                }

                leagues
            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}


