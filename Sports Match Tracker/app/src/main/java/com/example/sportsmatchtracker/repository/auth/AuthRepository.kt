package com.example.sportsmatchtracker.repository.auth

import com.example.sportsmatchtracker.model.auth.AuthError
import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.model.where.WhereCondition
import com.example.sportsmatchtracker.network.SocketManager
import com.example.sportsmatchtracker.repository.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository : Repository() {
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()
    val table = "uzytkownik"

    private fun parseServerError(message: String): AuthError {
        return when {
            message.contains("UNIQUE constraint failed: ${table}.email", ignoreCase = true) -> {
                AuthError(
                    errorMessage = "This email is already registered",
                    emailError = true
                )
            }

            message.contains("UNIQUE constraint failed: ${table}.nick", ignoreCase = true) -> {
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

//    private fun formLoginRequest(email: String, password: String): JSONObject {
//        return JSONObject().apply {
//            put("action", "SELECT")
//            put("table", "uzytkownik")
//            put("columns", JSONArray(listOf("email", "nick")))
//            put("where", JSONArray().apply {
//                put(JSONObject().apply {
//                    put("column", "email")
//                    put("operator", "=")
//                    put("value", email)
//                })
//                put(JSONObject().apply {
//                    put("column", "haslo")
//                    put("operator", "=")
//                    put("value", password)
//                })
//            })
//        }
//    }
//
//    private fun fromRegisterRequest(email: String, nick: String, password: String): JSONObject {
//        return JSONObject().apply {
//            put("action", "INSERT")
//            put("table", "uzytkownik")
//            put("columns", JSONArray(listOf("email", "nick", "haslo")))
//            put("values", JSONArray(listOf(email, nick, password)))
//        }
//    }

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
            table,
            listOf("email", "nick"),
            listOf(
                WhereCondition("email", "=", email),
                WhereCondition("haslo", "=", password)
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
                        nick = userJson.getString("nick"),
                        email = userJson.getString("email")
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
            table = "uzytkownik",
            columns = listOf("email", "nick", "haslo"),
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

    fun logout() {
        _userState.value = null
    }
}