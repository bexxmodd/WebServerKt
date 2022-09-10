package bexmod.http

import bexmod.WebLogger
import java.util.logging.Level

class HttpRequest(request: String) {
    var method = Method.UNINITIALIZED
        private set
    var version = Version.V1_1
        private set
    var resource = Resource("/")
        private set
    var headers = mutableMapOf<String, String>()
        private set
    var body = ""
        private set
    var isBadRequest = true
        private set

    init {
        request.lines()
            .forEach {
                if (it.contains("HTTP")) {
                    try {
                        val (m, v, r) = processRequest(it)
                        method = m
                        version = v
                        resource = r
                        isBadRequest = false
                    } catch (e: Exception) {
                        WebLogger.LOG.log(Level.SEVERE, "can't parse request line -> $e")
                    }
                } else if (it.contains(":")) {
                    val (key, value) = processHeader(it)
                    headers[key] = value
                } else if (it.isNotBlank()) {
                    body = it
                }
            }
    }

    private fun processRequest(line: String): Triple<Method, Version, Resource> {
        val pcs = line.split(" ")
        if (pcs.size != 3) {
            throw IllegalArgumentException("Check Request Line")
        }
        WebLogger.LOG.log(Level.INFO, "Request Line: $line")
        return Triple(
            Method.from(pcs[0]),
            Version.from(pcs[2]),
            Resource(pcs[1])
        )
    }

    private fun processHeader(line: String): Pair<String, String> {
        val pcs = line.split(":", limit = 2)
        return if (pcs.size > 1)
                    Pair(pcs[0].lowercase(), pcs[1].trim())
                else Pair(pcs[0].lowercase(), "")
    }
}