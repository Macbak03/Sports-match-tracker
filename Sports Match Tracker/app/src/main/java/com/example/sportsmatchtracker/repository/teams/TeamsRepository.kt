package com.example.sportsmatchtracker.repository.teams

import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.league.Team
import com.example.sportsmatchtracker.model.sport.Sport
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class TeamsRepository : Repository() {
    private val _teamsState = MutableStateFlow<List<Team>>(listOf())
    val teamsState: StateFlow<List<Team>> = _teamsState.asStateFlow()

    suspend fun fetchTeams() {
        val request = selectWithJoinRequest(
            table = DatabaseSchema.Teams.TABLE_NAME,
            columns = listOf(
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.NAME}",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.CITY}",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.NAME} as league_name",
                "${DatabaseSchema.Leagues.TABLE_NAME}.${DatabaseSchema.Leagues.COUNTRY} as league_country",
                "${DatabaseSchema.Sports.TABLE_NAME}.${DatabaseSchema.Sports.NAME} as sport_name"
            ),
            joins = listOf(
                JoinClause(
                    table = DatabaseSchema.Leagues.TABLE_NAME,
                    onLeft = "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.LEAGUE_NAME}",
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

                val teams = mutableListOf<Team>()

                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    teams.add(
                        Team(
                            name = row.getString("name"),
                            city = row.getString("city"),
                            league = League(
                                name = row.getString("league_name"),
                                country = row.getString("league_country"),
                                sport = Sport(
                                    name = row.getString("sport_name")
                                )
                            )
                        )
                    )
                }

                _teamsState.value = teams

            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
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

