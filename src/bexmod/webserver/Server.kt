package bexmod.webserver

import java.net.ServerSocket
import java.util.concurrent.Executors

val N_THREADS = Runtime.getRuntime().availableProcessors()
var FILE_DIRECTORY_PATH = ""
fun main(args: Array<String>) {
    if (args.size < 2) {
        throw IllegalArgumentException("Please provide Port number");
    }

    val portNumber: Int = args[0].toInt()
    FILE_DIRECTORY_PATH = args[1]
    val serverSocket = ServerSocket(portNumber)
    val poolExecutor = Executors.newFixedThreadPool(N_THREADS)
    println("<::[ Started Web Server - Listening on port:$portNumber ]::>")

    while (true) {
        val clientSocket = serverSocket.accept()
        println("Communicating with : ${clientSocket.inetAddress.hostAddress}")
        poolExecutor.execute(ConnectionHandler(clientSocket))
    }
}
