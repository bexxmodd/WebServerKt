package bexmod.webserver

import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import bexmod.http.Method
import java.net.Socket

class Router  {
    companion object {
        @JvmStatic
        fun route(req: HttpRequest, socket: Socket) {
            println("WAT? ${req.method}")
            val rsp = when (req.method) {
                Method.HEAD -> null
                Method.GET -> StaticPageHandler().handle(req)
//                    val routes = request.resource.path.split("/")
//                    when (routes[0]) {
//                        "file" -> {
//                            val rsp = StaticPageHandler().handle(request)
//                            socket.getOutputStream().write(rsp.toString().toByteArray())
//                        }
//                    }
                Method.PUT, Method.POST -> HttpResponse(405)
                Method.UNINITIALIZED -> if (req.isBadRequest) HttpResponse(400) else HttpResponse(501)
            }

            rsp?.let {
                socket.getOutputStream().write(rsp.toString().toByteArray())
                socket.getOutputStream().flush()
                println(rsp)
            }

        }
    }
}