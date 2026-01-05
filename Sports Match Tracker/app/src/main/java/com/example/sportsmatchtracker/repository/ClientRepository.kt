package com.example.sportsmatchtracker.repository
import com.example.sportsmatchtracker.model.Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.*
import java.net.Socket

class ClientRepository {
    private val _clientState = MutableStateFlow(Client())
    val clientState: StateFlow<Client> = _clientState.asStateFlow()

    suspend fun connectToServer(): Boolean {
        _clientState.update { it.copy(isLoading = true, connectionStatus = "Connecting...") }
        return try {
            withTimeout(3000) {
                withContext(Dispatchers.IO) {
                    var socket: Socket? = null
                    try {
                        socket = Socket()
                        socket.soTimeout = 3000
                        socket.connect(java.net.InetSocketAddress("172.26.0.3", 1100), 3000)
                        println("Socket connected")
                        
                        val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                        // get response from server
                        println("Waiting for server response...")
                        val response = input.readLine()
                        println("Server response: $response")

                        if (response == "connected") {
                            _clientState.update {
                                it.copy(
                                    connected = true,
                                    isLoading = false,
                                    connectionStatus = response
                                )
                            }
                            true
                        } else {
                            _clientState.update {
                                it.copy(
                                    connected = false,
                                    isLoading = false,
                                    connectionStatus = "Failed to connect to server"
                                )
                            }
                            false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        _clientState.update {
                            it.copy(
                                connected = false,
                                isLoading = false,
                                connectionStatus = "Connection failed: ${e.message}"
                            )
                        }
                        false
                    } finally {
                        socket?.close()
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            println("Connection timeout occurred")
            _clientState.update {
                it.copy(
                    connected = false,
                    isLoading = false,
                    connectionStatus = "Connection timeout - server not responding"
                )
            }
            false
        }
    }
}