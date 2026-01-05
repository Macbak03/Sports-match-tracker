package com.example.sportsmatchtracker.repository.auth

import com.example.sportsmatchtracker.model.user.User
import com.example.sportsmatchtracker.network.SocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class AuthRepository {
    private val _userState = MutableStateFlow<User?>(null)
    val userState: StateFlow<User?> = _userState.asStateFlow()

    private val socketManager = SocketManager.getInstance()

    suspend fun login(email: String, password: String): Boolean {
        if (!socketManager.isConnected) {
            println("Cannot login - not connected to server")
            return false
        }

        val request = JSONObject().apply {
            put("action", "SELECT")
            put("table", "uzytkownik")
            put("columns", JSONArray(listOf("email", "nick")))
            put("where", JSONArray().apply {
                put(JSONObject().apply {
                    put("column", "email")
                    put("operator", "=")
                    put("value", email)
                })
                put(JSONObject().apply {
                    put("column", "haslo")
                    put("operator", "=")
                    put("value", password)
                })
            })
        }

        val response = socketManager.sendRequestWithResponse(request)
        
        if (response == null) {
            println("No response from server")
            return false
        }

        return try {
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
                    println("Login successful: ${user.nick}")
                    true
                } else {
                    println("Invalid email or password")
                    false
                }
            } else {
                println("Server error: ${jsonResponse.optString("message")}")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error parsing response: ${e.message}")
            false
        }
    }

    suspend fun register(email: String, nick: String, password: String): Boolean {
        if (!socketManager.isConnected) {
            println("Cannot register - not connected to server")
            return false
        }

        val request = JSONObject().apply {
            put("action", "INSERT")
            put("table", "uzytkownik")
            put("columns", JSONArray(listOf("email", "nick", "haslo")))
            put("values", JSONArray(listOf(email, nick, password)))
        }

        val response = socketManager.sendRequestWithResponse(request)

        if (response == null) {
            println("No response from server")
            return false
        }

        return try {
            val jsonResponse = JSONObject(response)
            val status = jsonResponse.getString("status")

            if (status == "success") {
               login(email, password)
            } else {
                println("Server error: ${jsonResponse.optString("message")}")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error parsing response: ${e.message}")
            false
        }
    }
    
    fun logout() {
        _userState.value = null
    }
}