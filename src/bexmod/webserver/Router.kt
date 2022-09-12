package bexmod.webserver

import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import bexmod.http.Method
import java.net.Socket

class Router  {
    companion object {
        @JvmStatic
        fun route(req: HttpRequest, socket: Socket) {
            val rsp = when (req.method) {
                Method.HEAD, Method.GET -> {
                    val routes = req.resource.path.split("/")
                    when (routes[0]) {
                        "" -> StaticPageHandler().handle(req)
                        "api" -> DynamicPageHandler().handle(req)
                        "data" -> RawDataHandler().handle(req)
                        else -> null
                    }
                }
                Method.PUT, Method.POST -> HttpResponse(405)
                Method.UNINITIALIZED -> if (req.isBadRequest) HttpResponse(400) else HttpResponse(501)
            }

            rsp?.let { socket.getOutputStream().sendResponse(it) }
        }
    }
}