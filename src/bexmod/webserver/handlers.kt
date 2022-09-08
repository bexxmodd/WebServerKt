package bexmod.webserver

import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import bexmod.http.HttpWorker
import bexmod.http.Version
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class ConnectionHandler(private val socket: Socket) : Runnable {
    override fun run() {
        println("Accepted connection from: ${socket.inetAddress.hostAddress}:${socket.port}")
        val poolExecutor = Executors.newFixedThreadPool(N_THREADS)

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val buffer = StringBuilder()

        var bytesRead: Int
        val data = CharArray(16384)
        while (!socket.isInputShutdown) {
            bytesRead = reader.read(data, 0, data.size)
            if (bytesRead == -1) {
                socket.shutdownOutput()
                break
            }

            for (i in 0 until bytesRead)
                buffer.append(data[i])

            if (buffer.contains("\r\n\r\n"))
                poolExecutor.execute(HttpWorker(socket, buffer.toString()))
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
        val file = File(FILE_DIRECTORY_PATH + req.resource.path)
        if (req.isBadRequest) return HttpResponse(400)
        if (req.resource.path == "..") return HttpResponse(403)
        if (req.version != Version.V1_1) return HttpResponse(505)
        if (!file.exists() || file.isDirectory || file.isHidden) return HttpResponse(404)
        if (file.exists()) {
            return createRspWithBody(req, FILE_DIRECTORY_PATH + req.resource.path)
        }
        return HttpResponse(200)
    }

    private fun createRspWithBody(req: HttpRequest, path: String): HttpResponse {
        val headers = req.headers
        val fileModified = File(path).lastModified()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        if (headers.containsKey("If-Modified-Since")) {
            val sinceModified = headers["If-Modified-Since"]
            val d = dateFormat.parse(sinceModified)
            val timeSince = d.time
            if (timeSince > fileModified) {
                return HttpResponse(304)
            }
        }

        val data = loadFile(path, StandardCharsets.UTF_8)
        val newHeader = sortedMapOf<String, String>()
        val t: String = dateFormat.format(Date(fileModified))
        newHeader["Last-Modified"] = t
        newHeader["Content-type"] = "text/html"
        return HttpResponse(200, newHeader, Optional.of(data))
    }
}