package com.example.sportsmatchtracker.repository.auth

import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.model.database.WhereCondition
import com.example.sportsmatchtracker.repository.DatabaseSchema
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class AuthRepository : Repository() {
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()
    private val table = DatabaseSchema.Users

    private fun parseServerError(message: String): AuthError {
        return when {
            message.contains("UNIQUE constraint failed: ${table.TABLE_NAME}.${table.EMAIL}", ignoreCase = true) -> {
                AuthError(
                    errorMessage = "This email is already registered",
                    emailError = true
                )
            }

            message.contains("UNIQUE constraint failed: ${table.TABLE_NAME}.${table.NICK}", ignoreCase = true) -> {
                AuthError(
                    errorMessage = "This nick is already taken",
                    nickError = true
                )
            }

            message.contains("NOT NULL constraint failed", ignoreCase = true) -> {
                AuthError(
                    errorMessage = "All fields must be filled",
                    emailError = true,
                    passwordError = true
                )
            }

            message.contains("Invalid email or password", ignoreCase = true) -> {
                AuthError(
                    errorMessage = "Invalid email or password",
                    emailError = true,
                    passwordError = true
                )
            }

            else -> {
                AuthError(
                    errorMessage = "Unidentified error: $message"
                )
            }
        }
    }

    suspend fun login(email: String, password: String) {
        if (!socketManager.isConnected) {
            throw AuthError(errorMessage = "No connection to server", generalError = true)
        }

        if (email.isBlank()) {
            throw AuthError(errorMessage = "Email cannot be empty", emailError = true)
        }
        if (password.isBlank()) {
            throw AuthError(errorMessage = "Password cannot be empty", passwordError = true)
        }

        val request = selectRequest(
            table.TABLE_NAME,
            listOf(table.EMAIL, table.NICK),
            listOf(
                WhereCondition(table.EMAIL, "=", email),
                WhereCondition(table.PASSWORD, "=", password)
            )
        )
        val response = socketManager.sendRequestWithResponse(request) ?: throw AuthError(
            errorMessage = "No response from server",
            generalError = true
        )

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                val data = jsonResponse.getJSONArray("data")
                val count = jsonResponse.getInt("count")

                if (count > 0) {
                    val userJson = data.getJSONObject(0)
                    val user = User(
                        nick = userJson.getString(table.NICK),
                        email = userJson.getString(table.EMAIL)
                    )
                    _userState.value = user

                } else {
                    throw AuthError(
                        errorMessage = "Invalid email or password",
                        passwordError = true,
                        emailError = true
                    )
                }
            } else {
                throw AuthError(
                    errorMessage = jsonResponse.optString("message"),
                    generalError = true
                )
            }
        } catch (e: AuthError) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthError(
                errorMessage = "Error when trying to login: ${e.message}",
                generalError = true
            )
        }
    }

    suspend fun register(email: String, nick: String, password: String) {
        if (!socketManager.isConnected) {
            throw AuthError(errorMessage = "No connection to server", generalError = true)
        }

        if (email.isBlank()) {
            throw AuthError(errorMessage = "Email cannot be empty", emailError = true)
        }
        if (nick.isBlank()) {
            throw AuthError(errorMessage = "Nick cannot be empty", nickError = true)
        }
        if (password.isBlank()) {
            throw AuthError(errorMessage = "Password cannot be empty", passwordError = true)
        }

        val request = insertRequest(
            table = table.TABLE_NAME,
            columns = listOf(table.EMAIL, table.NICK, table.PASSWORD),
            values = listOf(email, nick, password)
        )
        val response = socketManager.sendRequestWithResponse(request) ?: throw AuthError(
            errorMessage = "No response from server",
            generalError = true
        )

        try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
                login(email, password)
            } else {
                val errorMessage = jsonResponse.optString("message", "Unknown error")
                throw parseServerError(errorMessage)
            }
        } catch (e: AuthError) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw AuthError(
                errorMessage = "Error when trying to register: ${e.message}",
                generalError = true
            )
        }
    }

    fun setUserState(user: User?) {
        _userState.value = user
    }
    fun logout() {
        _userState.value = null
    }
}