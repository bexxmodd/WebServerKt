package bexmod.webserver

import bexmod.WebLogger
import bexmod.http.HttpResponse
import bexmod.http.HttpWorker
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.logging.Level

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        WebLogger.LOG.log(Level.SEVERE,
            "Port number where Socker Server should run is not supplied")
        throw IllegalArgumentException("Please provide Port number");
    }
    val portNumber: Int = args[0].toInt()
    Server(portNumber).run()
}

class Server(private val port: Int) {
    companion object {
        val N_THREADS = Runtime.getRuntime().availableProcessors()
    }
    fun run() {
        val poolExecutor = Executors.newFixedThreadPool(N_THREADS)
        val serverSocket = ServerSocket(port)
        WebLogger.LOG.log(Level.INFO, "Started Web Server - Listening on port : $port")

        while (true) {
            val socket = serverSocket.accept()
            poolExecutor.execute {
                WebLogger.LOG.log(Level.INFO,
                    "Accepted connection from: ${socket.inetAddress.hostAddress}:${socket.port}")

                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                var bytesRead: Int
                val data = CharArray(16384)
                val buffer = StringBuilder()

                while (!socket.isInputShutdown) {
                    bytesRead = reader.read(data, 0, data.size)
                    if (bytesRead == -1) {
                        socket.shutdownOutput()
                        WebLogger.LOG.log(Level.WARNING, "Shutting down output on : $socket")
                        break
                    }

                    for (i in 0 until bytesRead) buffer.append(data[i])

                    if (buffer.contains("\r\n\r\n"))
                        poolExecutor.execute(HttpWorker(socket, buffer.toString()))
                }
                WebLogger.LOG.log(Level.WARNING,
                    "Closing connection with ${socket.inetAddress.hostAddress}:${socket.port}")
                socket.close()
            }
        }
    }
}
fun OutputStream.sendResponse(rsp: HttpResponse) {
    WebLogger.LOG.log(
        Level.INFO,
        "\n\t\tResponse Line: ${rsp.version} ${rsp.statusCode} ${rsp.statusText}" +
                "\n\t\tHeaders: ${rsp.headers}\n")
    write(rsp.toString().toByteArray())
    flush()
}
