package bexmod.webserver

import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import bexmod.http.Method
import java.net.Socket

class Router  {
    companion object {
        @JvmStatic
        fun route(httpRequest: HttpRequest, socket: Socket) {
            if (httpRequest.method == Method.HEAD) return
//            HttpResponse httpResponse = StaticWebHandler.handle(httpRequest)
//            socket.getOutputStream().write(httpResponse.toString().getBytes)
        }
    }
}