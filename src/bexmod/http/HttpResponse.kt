package bexmod.http

import bexmod.WebLogger
import java.io.OutputStream
import java.util.*
import java.util.logging.Level

class HttpResponse() {
    private var version = Version.V1_1
    private var statusCode = 200
    private var statusText = "Success"
    private var headers: SortedMap<String, String> = sortedMapOf()
    private var body: Optional<String> = Optional.empty()


    constructor(status: Int) : this() {
        if (status != 200) {
            statusCode = status
            statusText = statusTextFrom(status)
        }
        headers["Content-Length"] = "0"
    }

    constructor(
        status: Int,
        rspHeaders: SortedMap<String, String>,
        rspBody: Optional<String>
    ) : this() {
        if (status != 200) {
            statusCode = status
            statusText = statusTextFrom(status)
        }

        if (rspBody.isPresent) {
            body = rspBody
        }

        headers = rspHeaders
        headers["Content-Length"] =
            (if (body.isPresent) body.get().length else 0).toString()
    }

    override fun toString(): String {
        return "${version.version} $statusCode $statusText\r\n" +
                "${headersAsStrings()}\r\n" +
                if (body.isPresent) body.get() else ""
    }

    private fun headersAsStrings(): String {
        val sb = StringBuilder()
        headers.forEach {
            (k, v) ->
                sb.append(k)
                sb.append(": ")
                sb.append(v)
                sb.append("\r\n")
        }
        return sb.toString()
    }

    fun sendResponse(stream: OutputStream) {
        val rsp = this.toString()
        WebLogger.LOG.log(
            Level.INFO,
            "Returning Rsp: ${version.version} $statusCode $statusText")
        stream.write(rsp.toByteArray())
    }
}