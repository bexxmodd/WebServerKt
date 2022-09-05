package bexmod.webserver

import bexmod.http.HttpWorker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import kotlin.concurrent.thread

class ConnectionHandler(private val socket: Socket) {
    fun run() {
        while (true) {
            try {
                val reader
                    = BufferedReader(InputStreamReader(socket.getInputStream()))

                var nRead = 0
                val data = CharArray(16000)
                do {
                    nRead = reader.read(data, 0, data.size)
                    val buffer = StringBuilder()
                    buffer.append(String(data))

                    if (buffer.contains("\r\n\r\n")) {
                        thread { HttpWorker(socket, buffer.toString()) }
                    }
                } while (nRead != -1)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }
}