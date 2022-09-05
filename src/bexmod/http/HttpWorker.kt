package bexmod.http

import java.net.Socket

import bexmod.webserver.Router

class HttpWorker(private val socket: Socket, private val request: String) {
    fun run() {
        try {
            val httpRequest = HttpRequest(request)
            Router.route(httpRequest, socket)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}