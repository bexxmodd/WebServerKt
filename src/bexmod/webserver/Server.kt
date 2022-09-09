package bexmod.webserver

import bexmod.WebLogger
import java.net.ServerSocket
import java.util.concurrent.Executors
import java.util.logging.Level

class Server(private val port: Int) {
    companion object {
        val N_THREADS = Runtime.getRuntime().availableProcessors()
    }
    fun run() {
        val poolExecutor = Executors.newFixedThreadPool(N_THREADS)
        val serverSocket = ServerSocket(port)
        WebLogger.LOG.log(Level.INFO, "Started Web Server - Listening on port : $port")

        while (true) {
            val clientSocket = serverSocket.accept()
            poolExecutor.execute(ConnectionHandler(clientSocket))
        }
    }
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Please provide Port number");
    }
    val portNumber: Int = args[0].toInt()
    val dir = args[1]
    Server(portNumber).run()
}
