package com.example.sportsmatchtracker.repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.*
import java.net.Socket

class ClientRepository {
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    fun connectToServer() {
        Thread {
            try {
                val socket = Socket("172.18.24.78", 1100) // IP serwera
                val output = PrintWriter(
                    BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                    true
                )
                val input = BufferedReader(InputStreamReader(socket.getInputStream()))

                _connected.value = true

                // wysyłanie danych
                output.println("Hello from Android")

                // odbieranie danych
                val response = input.readLine()
                println("Server response: $response")

                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                _connected.value = false
            }
        }.start()
    }
}