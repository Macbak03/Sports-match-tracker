package com.example.sportsmatchtracker.repository.subscriptions

import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.subscriptions.LeagueSubscription
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class LeagueSubscriptionsRepository : SubscriptionsRepository() {
    private val _subscriptionsState = MutableStateFlow<List<LeagueSubscription>>(emptyList())
    val subscriptionsState: StateFlow<List<LeagueSubscription>> = _subscriptionsState.asStateFlow()

    override fun parseServerError(message: String): Exception {
        return when {
            message.contains(
                "UNIQUE constraint failed: ${""}",
                ignoreCase = true
            ) -> {
                Exception(
                    "Failed to add subscription to user with that email"
                )
            }

            message.contains(
                "UNIQUE constraint failed: ${""}",
                ignoreCase = true
            ) -> {
                Exception(
                    "Failed to add subscription for this league"
                )
            }

            else -> {
                Exception(
                    "Unidentified error: $message"
                )
            }
        }
    }

    override suspend fun fetchSubscriptions(userEmail: String) {
        val request = selectRequest(
            table = DatabaseSchema.LeagueSubscriptions.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.LeagueSubscriptions.LEAGUE_NAME,
                DatabaseSchema.LeagueSubscriptions.LEAGUE_COUNTRY
            ),
            where = listOf(
                WhereCondition(
                    column = DatabaseSchema.LeagueSubscriptions.SUBSCRIBER_EMAIL,
                    operator = "=",
                    value = userEmail
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

                val subscriptions = mutableListOf<LeagueSubscription>()

                for (i in 0 until count) {
                    try {
                        val row = data.getJSONObject(i)
                        subscriptions.add(
                            LeagueSubscription(
                                leagueName = row.getString(DatabaseSchema.LeagueSubscriptions.LEAGUE_NAME),
                                leagueCountry = row.getString(DatabaseSchema.LeagueSubscriptions.LEAGUE_COUNTRY)
                            )
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                _subscriptionsState.value = subscriptions

            } else {
                throw Exception(jsonResponse.optString("message"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun addSubscription(userEmail: String, subscription: LeagueSubscription) {
        val request = insertRequest(
            table = DatabaseSchema.LeagueSubscriptions.TABLE_NAME,
            columns = listOf(
                DatabaseSchema.LeagueSubscriptions.SUBSCRIBER_EMAIL,
                DatabaseSchema.LeagueSubscriptions.LEAGUE_NAME,
                DatabaseSchema.LeagueSubscriptions.LEAGUE_COUNTRY
            ),
            values = listOf(
                userEmail,
                subscription.leagueName,
                subscription.leagueCountry
            )
        )

        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                this.fetchSubscriptions(userEmail)
            } else {
                val errorMessage = jsonResponse.optString("message", "Unknown error")
                throw parseServerError(errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun unsubscribe(userEmail: String, subscription: LeagueSubscription) {
        val request = deleteRequest(
            table = DatabaseSchema.LeagueSubscriptions.TABLE_NAME,
            where = listOf(
                WhereCondition(
                    column = DatabaseSchema.LeagueSubscriptions.SUBSCRIBER_EMAIL,
                    operator = "=",
                    value = userEmail
                ),
                WhereCondition(
                    column = DatabaseSchema.LeagueSubscriptions.LEAGUE_NAME,
                    operator = "=",
                    value = subscription.leagueName
                ),
                WhereCondition(
                    column = DatabaseSchema.LeagueSubscriptions.LEAGUE_COUNTRY,
                    operator = "=",
                    value = subscription.leagueCountry
                )
            )
        )

        val response = socketManager.sendRequestWithResponse(request)
            ?: throw Exception("No response from server")
        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                this.fetchSubscriptions(userEmail)
            } else {
                val errorMessage = jsonResponse.optString("message", "Unknown error")
                throw parseServerError(errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}