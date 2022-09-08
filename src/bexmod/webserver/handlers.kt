package bexmod.webserver

import bexmod.http.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread

class ConnectionHandler(private val socket: Socket) {
    fun run() {
        println("Accepted connection from: ${socket.inetAddress.hostAddress}:${socket.port}")

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val buffer = StringBuilder()

        var bytesRead: Int
        val data = CharArray(16384)
        while (!socket.isInputShutdown) {
            bytesRead = reader.read(data, 0, data.size)
            if (bytesRead == -1) {
                socket.shutdownOutput()
                continue
            }

            for (i in 0 until bytesRead)
                buffer.append(data[i])

            if (buffer.contains("\r\n\r\n"))
                thread { HttpWorker(socket, buffer.toString()).run() }
        }
        println("Closing connection with ${socket.inetAddress.hostAddress}:${socket.port}")
        socket.close()
    }
}

interface Handler {
    fun handle(req: HttpRequest): HttpResponse
    fun loadFile(path: String, encoding: Charset): String {
        val encoded = Files.readAllBytes(Paths.get(path))
        return String(encoded, encoding)
    }
}

class StaticPageHandler : Handler {
    override fun handle(req: HttpRequest): HttpResponse {
        val file = File(req.resource.path)

//        if (req.isBadRequest) return HttpResponse(400)
//        if (req.resource.path == "..") return HttpResponse(403)
//        if (req.method == Method.POST || req.method == Method.PUT) return HttpResponse(405)
//        if (req.method == Method.UNINITIALIZED) return HttpResponse(501)
//        if (req.version != Version.V1_1) return HttpResponse(505)
//        if (!file.exists() || file.isDirectory || file.isHidden) return HttpResponse(404)
        return HttpResponse(200)
    }
}