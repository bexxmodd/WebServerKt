package bexmod.webserver

import bexmod.http.HttpWorker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.nio.Buffer
import java.util.*
import kotlin.concurrent.thread

class ConnectionHandler(private val socket: Socket) {
    fun run() {
        println("Accepted connection from: ${socket.inetAddress.hostAddress}:${socket.port}")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val buffer = StringBuilder()

        var bytesRead: Int
        val data = CharArray(16400)
        while (!socket.isInputShutdown) {
            bytesRead = reader.read(data, 0, data.size)
            if (bytesRead == -1) {
                socket.shutdownOutput()
            }
            data.forEach(buffer::append)

            if (buffer.contains("\r\n\r\n"))
                thread { HttpWorker(socket, buffer.toString()) }
        }
        println("Closing connection with ${socket.inetAddress.hostAddress}:${socket.port}")
        socket.close()
    }
}