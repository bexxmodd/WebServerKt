package bexmod.http

import java.util.*
import kotlin.collections.HashMap

class HttpResponse() {
    var version = Version.V1_1
    private var statusCode = 200
    private var statusText = "Success"
    var headers: Optional<SortedMap<String, String>> = Optional.empty()
    var body: Optional<String> = Optional.empty()

    constructor(status: Int) : this() {
        if (status != 200) {
            statusCode = status
            statusText = statusTextFrom(status)
        }
    }

    constructor(
        status: Int,
        rspHeaders: Optional<SortedMap<String, String>>,
        rspBody: Optional<String>
    ) : this() {
        if (status != 200) {
            statusCode = status
            statusText = statusTextFrom(status)
        }

        if (rspBody.isPresent) {
            body = rspBody
        }

        if (rspHeaders.isPresent) {
            headers = rspHeaders
            headers.get()["Content-Length"] =
                (if (body.isPresent) body.get().length else 0).toString()
        }
    }

    override fun toString(): String {
        return "${version.version} $statusCode $statusText\r\n" +
                "${headersAsStrings()}\r\n" +
                if (body.isPresent) body.get() + "\r\n\r\n" else ""
    }

    private fun headersAsStrings(): String {
        val sb = StringBuilder()
        headers.ifPresent {
            it.forEach {
                (k, v) ->
                    sb.append(k)
                    sb.append(": ")
                    sb.append(v)
                    sb.append("\r\n")
            }
        }
        return sb.toString()
    }
}