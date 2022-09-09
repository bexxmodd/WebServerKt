package bexmod.webserver

import bexmod.WebLogger
import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import bexmod.http.HttpWorker
import bexmod.http.Version
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.Socket
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.logging.Level

class ConnectionHandler(private val socket: Socket) : Runnable {
    override fun run() {
        WebLogger.LOG.log(Level.INFO,
            "Accepted connection from: ${socket.inetAddress.hostAddress}:${socket.port}")
        val poolExecutor = Executors.newFixedThreadPool(Server.N_THREADS)

        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val buffer = StringBuilder()

        var bytesRead: Int
        val data = CharArray(16384)
        while (!socket.isInputShutdown) {
            bytesRead = reader.read(data, 0, data.size)
            if (bytesRead == -1) {
                socket.shutdownOutput()
                WebLogger.LOG.log(Level.WARNING, "Shutting down output on : $socket")
                break
            }

            for (i in 0 until bytesRead)
                buffer.append(data[i])

            if (buffer.contains("\r\n\r\n"))
                poolExecutor.execute(HttpWorker(socket, buffer.toString()))
        }
        WebLogger.LOG.log(Level.WARNING,
            "Closing connection with ${socket.inetAddress.hostAddress}:${socket.port}")
        socket.close()
    }
}

interface Handler {

    fun handle(req: HttpRequest): HttpResponse

    companion object {
        fun loadFile(path: String, encoding: Charset = Charsets.UTF_8): Optional<String> {
            val f = File(path)
            if (f.exists())
                return Optional.of(String(f.readBytes(), encoding))
            WebLogger.LOG.log(Level.WARNING, "Can't load file from $path")
            return Optional.empty()
        }
    }
}

class StaticPageHandler : Handler {
    private val STATIC = "static"

    override fun handle(req: HttpRequest): HttpResponse {
        WebLogger.LOG.log(Level.INFO, "Static Page Request")
        if (req.isBadRequest) return HttpResponse(400)
        if (req.resource.path == "..") return HttpResponse(403)
        if (req.version != Version.V1_1) return HttpResponse(505)

        val routes = req.resource.path.split("/")
        val path = routes[1]
        val headers = sortedMapOf<String, String>()
        return when (path) {
            "" -> HttpResponse(200, headers, Handler.loadFile("Resources/$STATIC/index.html"))
            "health" -> HttpResponse(200)
            path -> {
                if (Handler.loadFile("Resources/$STATIC/$path").isPresent) {
                    if (path.endsWith(".html"))
                        headers["Content-Type"] = "text/html"

                    if (path.endsWith(".css"))
                        headers["Content-Type"] = "text/css"

                    if (path.endsWith(".js"))
                        headers["Content-Type"] = "text/javascript"

                    HttpResponse(200, headers, Handler.loadFile("Resources/$STATIC/$path"))
                } else HttpResponse(404, headers, Handler.loadFile("Resources/$STATIC/404.html"))
            }
            else -> HttpResponse(200)
        }
    }

}

class DynamicPageHandler() : Handler {
    override fun handle(req: HttpRequest): HttpResponse {
        WebLogger.LOG.log(Level.WARNING, "I'm Dynamic")
        TODO("Not yet implemented")
    }
}

class RawDataHandler() : Handler {

    override fun handle(req: HttpRequest): HttpResponse {
        WebLogger.LOG.log(Level.INFO, "Handling raw data request")
        val path = req.resource.path
        val routes = path.split("/")
        return when (routes[1]) {
            "file1.txt" -> createRspWithBody(req, path)
            else -> HttpResponse(404)
        }
    }
    private fun createRspWithBody(req: HttpRequest, path: String): HttpResponse {
        val file = File(path)
        if (file.exists() || file.isHidden) {
            return HttpResponse(404)
        }

        val fileModified = file.lastModified()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        val headers = req.headers
        if (headers.containsKey("If-Modified-Since")) {
            val sinceModified = headers["If-Modified-Since"]
            val d = dateFormat.parse(sinceModified)
            if (d.time > fileModified) {
                return HttpResponse(304)
            }
        }

        val data = Handler.loadFile(path)
        val newHeader = sortedMapOf<String, String>()
        val t: String = dateFormat.format(Date(fileModified))
        newHeader["Last-Modified"] = t
        newHeader["Content-type"] = "text/html"
        return HttpResponse(200, newHeader, Optional.of(data.get()))
    }
}