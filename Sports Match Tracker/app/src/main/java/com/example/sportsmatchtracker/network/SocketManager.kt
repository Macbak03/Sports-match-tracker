package com.example.sportsmatchtracker.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.io.*
import java.net.Socket

class SocketManager private constructor() {
    private var socket: Socket? = null
    private var input: BufferedReader? = null
    private var output: PrintWriter? = null
    
    var isConnected: Boolean = false
        private set

    companion object {
        @Volatile
        private var instance: SocketManager? = null

        fun getInstance(): SocketManager {
            return instance ?: synchronized(this) {
                instance ?: SocketManager().also { instance = it }
            }
        }
    }
                                        //172.30.0.236    172.26.0.3
    suspend fun connect(host: String = "172.26.0.3", port: Int = 1100): Boolean {
        return try {
            withTimeout(3000) {
                withContext(Dispatchers.IO) {
                    try {
                        // Close existing socket if any
                        disconnect()

                        socket = Socket()
                        socket?.soTimeout = 3000
                        socket?.connect(java.net.InetSocketAddress(host, port), 3000)
                        println("Socket connected")

                        input = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                        output = PrintWriter(
                            BufferedWriter(OutputStreamWriter(socket!!.getOutputStream())),
                            true
                        )

                        // Wait for initial server response
                        val response = input?.readLine()
                        println("Server response: $response")

                        if (response == "connected") {
                            isConnected = true
                            true
                        } else {
                            disconnect()
                            false
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        disconnect()
                        false
                    }
                }
            }
        } catch (_: TimeoutCancellationException) {
            println("Connection timeout occurred")
            disconnect()
            false
        }
    }

    suspend fun sendRequestWithResponse(jsonRequest: JSONObject): String? {
        if (!isConnected) {
            println("Socket not connected")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                println("Sending request: $jsonRequest")
                output?.println(jsonRequest.toString())

                val response = input?.readLine()
                println("Received response: $response")
                response
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun disconnect() {
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        socket = null
        input = null
        output = null
        isConnected = false
    }
}
