package bexmod.http

import bexmod.webserver.Router
import java.net.Socket

class HttpWorker(private val socket: Socket, private val request: String) : Runnable {

    override fun run() {
        val httpRequest = HttpRequest(request)
        Router.route(httpRequest, socket)
    }
}