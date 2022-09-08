package bexmod.webserver

import bexmod.http.HttpRequest
import bexmod.http.Method
import java.net.Socket

class Router  {
    companion object {
        @JvmStatic
        fun route(request: HttpRequest, socket: Socket) {
            println("WAT? ${request.method}")
            when (request.method) {
                Method.HEAD -> return
                Method.GET -> {
                    val rsp = StaticPageHandler().handle(request)
                    socket.getOutputStream().write(rsp.toString().toByteArray())
//                    val routes = request.resource.path.split("/")
//                    when (routes[0]) {
//                        "file" -> {
//                            val rsp = StaticPageHandler().handle(request)
//                            socket.getOutputStream().write(rsp.toString().toByteArray())
//                        }
//                    }
                }
                else -> return
            }
        }
    }
}