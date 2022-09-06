package bexmod.http

enum class Method(val method: String) {
    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    UNINITIALIZED("UNINITIALIZED");

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

enum class Version(val version: String) {
    V1_1("HTTP/1.1"),
    V2_0("HTTP/2.0"),
    VNA("NA");

    fun from(v: String): Version {
        return when (v) {
            "HTTP/1.1" -> V1_1
            "HTTP/2.0" -> V2_0
            else -> VNA
        }
    }
}

data class Resource(private val path: String) {

    fun getPath(): String {
        return path
    }

}

