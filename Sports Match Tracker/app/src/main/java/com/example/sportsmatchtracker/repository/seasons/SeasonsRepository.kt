package com.example.sportsmatchtracker.repository.seasons

import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.formatter.CustomDateFormatter
import com.example.sportsmatchtracker.model.league.League
import com.example.sportsmatchtracker.model.table.Season
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

class SeasonsRepository: Repository() {
    suspend fun fetchSeasonsForLeague(league: League): List<Season> {
        val request = selectRequest(
            table = DatabaseSchema.Seasons.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.Seasons.DATE_START,
                DatabaseSchema.Seasons.DATE_END
            ),
            where = listOf(
                WhereCondition(
                    column = DatabaseSchema.Seasons.LEAGUE_NAME,
                    operator = "=",
                    value = league.name
                ),
                WhereCondition(
                    column = DatabaseSchema.Seasons.LEAGUE_COUNTRY,
                    operator = "=",
                    value = league.country
                )
            )
        )

        val response = socketManager.sendRequestWithResponse(request) ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")
            val seasons = mutableListOf<Season>()
            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")
                for (i in 0 until count) {
                    val row = data.getJSONObject(i)
                    val startDateString = row.getString(DatabaseSchema.Seasons.DATE_START)
                    val endDateString = row.getString(DatabaseSchema.Seasons.DATE_END)
                    val startDate = LocalDate.parse(startDateString, CustomDateFormatter.DATE)
                    val endDate = LocalDate.parse(endDateString, CustomDateFormatter.DATE)
                    seasons.add(
                        Season(
                            dateStart = startDate,
                            dateEnd = endDate
                        )
                    )
                }
            }
            return seasons
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}