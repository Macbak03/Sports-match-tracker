package com.example.sportsmatchtracker.repository.settings
import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class SettingsRepository : Repository(){

    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()
    private val table = DatabaseSchema.Users

    private fun parseServerError(message: String): Exception {
        return when {
            message.contains(
                "UNIQUE constraint failed: users.nick",
                ignoreCase = true
            ) -> {
                Exception(
                    "Nick is already in use"
                )
            }

            else -> {
                Exception(
                    "Unidentified error: $message"
                )
            }
        }
    }

    suspend fun updateUserNick(email: String, newNick: String) = withConnectionCheck {
        if (!socketManager.isConnected) {
            throw Exception("No connection to server")
        }

        if (newNick.isBlank()) {
            throw Exception("Nick cannot be empty")
        }

        val request = updateRequest(
            table = table.TABLE_NAME,
            columns = listOf(table.NICK),
            values = listOf(newNick),
            where = listOf(WhereCondition(table.EMAIL, "=", email))
        )

        val response = socketManager.sendRequestWithResponse(request) ?: throw Exception("No response from server")

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                // Update local user state
                _userState.value = _userState.value?.copy(nick = newNick)
            } else {
                val errorMessage = jsonResponse.optString("message", "Unknown error")
                throw parseServerError(errorMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
    fun setUserState(user: User?) {
        _userState.value = user
    }
    fun logout() {
        _userState.value = null
    }
}