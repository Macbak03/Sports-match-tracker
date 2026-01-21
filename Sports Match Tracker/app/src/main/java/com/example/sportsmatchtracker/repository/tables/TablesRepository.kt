package com.example.sportsmatchtracker.repository.tables

import com.example.sportsmatchtracker.model.database.JoinClause
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.table.Season
import com.example.sportsmatchtracker.model.table.TableStanding
import com.example.sportsmatchtracker.model.team.Team
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import org.json.JSONObject

class TablesRepository: Repository() {

    suspend fun fetchSeasonTable(season: Season, league: League): List<TableStanding> = withConnectionCheck {
        val request = selectWithJoinRequest(
            table = DatabaseSchema.PositionsInTable.TABLE_NAME,
            columns = listOf(
                "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.DRAWS} as draws",
                "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.LOSSES} as losses",
                "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.WINS} as wins",
                "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.MATCHES_PLAYED} as matches_played",
                "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.POINTS} as points",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.NAME} as team_name",
                "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.CITY} as team_city"
            ),
            joins = listOf(
                JoinClause(
                    table = DatabaseSchema.Teams.TABLE_NAME,
                    onLeft = "${DatabaseSchema.PositionsInTable.TABLE_NAME}.${DatabaseSchema.PositionsInTable.TEAM_NAME}",
                    onRight = "${DatabaseSchema.Teams.TABLE_NAME}.${DatabaseSchema.Teams.NAME}"
                )
            ),
            where = listOf(
                WhereCondition(
                    column = DatabaseSchema.PositionsInTable.TABLE_SEASON_START_DATE,
                    operator = "=",
                    value = season.dateStart
                ),
                WhereCondition(
                    column = DatabaseSchema.PositionsInTable.TABLE_SEASON_END_DATE,
                    operator = "=",
                    value = season.dateEnd
                ),
                WhereCondition(
                    column = DatabaseSchema.PositionsInTable.TABLE_SEASON_LEAGUE_NAME,
                    operator = "=",
                    value = league.name
                ),
                WhereCondition(
                    column = DatabaseSchema.PositionsInTable.TABLE_SEASON_LEAGUE_COUNTRY,
                    operator = "=",
                    value = league.country
                ),
            ),
            orderBy = DatabaseSchema.PositionsInTable.POINTS,
            orderDirection = "DESC"
        )

        val response = socketManager.sendRequestWithResponse(request) ?: throw Exception("No response from server")

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")
            val tableStandings = mutableListOf<TableStanding>()
            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")
                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    val team = Team(
                        name = row.getString("team_name"),
                        city = row.getString("team_city")
                    )
                    tableStandings.add(
                        TableStanding(
                            team = team,
                            draws = row.getInt("draws"),
                            losses = row.getInt("losses"),
                            wins = row.getInt("wins"),
                            matchesPlayed = row.getInt("matches_played"),
                            points = row.getInt("points")
                        )
                    )
                }

                if (tableStandings.isEmpty()) {
                    // Fetch all teams for the league if no table data
                    val teams = fetchTeamsForLeague(league)
                    teams.forEach { team ->
                        tableStandings.add(
                            TableStanding(
                                team = team,
                                draws = 0,
                                losses = 0,
                                wins = 0,
                                matchesPlayed = 0,
                                points = 0
                            )
                        )
                    }
                }
            }
            tableStandings
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private suspend fun fetchTeamsForLeague(league: League): List<Team> {
        val request = selectRequest(
            table = DatabaseSchema.Teams.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.Teams.NAME,
                DatabaseSchema.Teams.CITY
            ),
            where = listOf(
                WhereCondition(DatabaseSchema.Teams.LEAGUE_NAME, "=", league.name),
                WhereCondition(DatabaseSchema.Teams.LEAGUE_COUNTRY, "=", league.country)
            )
        )
        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        
        val teams = mutableListOf<Team>()
        val jsonResponse = JSONObject(response)
        if (jsonResponse.getString("status") == "success") {
            val data = jsonResponse.getJSONArray("data")
            for (i in 0 until jsonResponse.getInt("count")) {
                val row = data.getJSONObject(i)
                teams.add(
                    Team(
                        name = row.getString(DatabaseSchema.Teams.NAME),
                        city = row.getString(DatabaseSchema.Teams.CITY)
                    )
                )
            }
        }
        return teams
    }
}