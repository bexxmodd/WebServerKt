package bexmod.webserver

import java.net.ServerSocket
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        throw IllegalArgumentException("Please provide Port number");
    }

    val portNumber: Int = args[0].toInt()
    val serverSocket = ServerSocket(portNumber)
    println("<::[ Started Web Server - Listening on port:$portNumber ]::>")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Communicating with : ${clientSocket.inetAddress.hostAddress}")
        thread { ConnectionHandler(clientSocket).run() }
    }
}
