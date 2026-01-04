package com.example.sportsmatchtracker.repository
import com.example.sportsmatchtracker.model.Client
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.net.Socket

class ClientRepository {
    private val _clientState = MutableStateFlow(Client())
    val clientState: StateFlow<Client> = _clientState.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO)

    fun connectToServer() {
        scope.launch {
            var socket: Socket? = null
            try {
                socket = Socket("172.26.0.3", 1100)
                val output = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    true
                )
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                withContext(Dispatchers.Main) {
                    _clientState.update { it.copy(
                        connected = true,
                        connectionStatus = "Connected to server"
                    )}
                }

                // Create example request
                val sqlRequest = JSONObject().apply {
                    put("type", "query")
                    put("action", "SELECT")
                    put("table", "uzytkownik")
                    put(
                        "columns",
                        org.json.JSONArray(listOf("email", "nick", "haslo"))
                    )
                    put("where", JSONObject().apply {
                        put("column", "nick")
                        put("operator", "=")
                        put("value", "macbak")
                    })
                    put("limit", 10)
                }

                // Send JSON to server
                println("Sending to server: $sqlRequest")
                output.println(sqlRequest.toString())

                // get response from server
                val response = input.readLine()
                println("Server response: $response")
                
                withContext(Dispatchers.Main) {
                    _clientState.update { it.copy(
                        responseFromServer = response ?: "No response"
                    )}
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _clientState.update { it.copy(
                        connected = false,
                        connectionStatus = "Connection failed: ${e.message}",
                    )}
                }
            } finally {
                socket?.close()
            }
        }
    }
}