package bexmod.http

enum class Method(val method: String) {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    UNINITIALIZED("UNINITIALIZED");

    companion object {
        fun from(m: String): Method {
            return when (m) {
                "GET" -> GET
                "HEAD" -> HEAD
                "POST" -> POST
                "PUT" -> PUT
                else -> UNINITIALIZED
            }
        }
    }
}

enum class Version(val version: String) {
    V0_9("HTTP/0.9"),
    V1_1("HTTP/1.1"),
    V2_0("HTTP/2.0");

    companion object {
        fun from(v: String): Version {
            return when (v) {
                "HTTP/1.1" -> V1_1
                "HTTP/2.0" -> V2_0
                else -> V0_9
            }
        }
    }
}

data class Resource(val path: String)

fun statusTextFrom(code: Int) = when(code) {
        200 -> "Succeess"
        304 -> "Not Modified"
        400 -> "Bad Request"
        403 -> "Forbidden"
        404 -> "Not Found"
        405 -> "Not Allowed"
        501 -> "Not Implemented"
        505 -> "HTTP Version Not Supported"
        else -> throw IllegalArgumentException("Code $code is invalid")
    }

