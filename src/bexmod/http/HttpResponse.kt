package bexmod.http

import bexmod.WebLogger
import java.io.OutputStream
import java.util.*
import java.util.logging.Level

class HttpResponse() {
    private var version = Version.V1_1
    private var statusCode = 200
    private var statusText = "Success"
    private var headers: SortedMap<String, String>
        = sortedMapOf(
            "Content-Type" to "application/octet-stream",
            "Content-Length" to "0",
            "Server" to "Bexx@${System.getProperty("os.name")}"
        )
    private var body: Optional<String> = Optional.empty()
    private var onlyHead = false


    constructor(status: Int) : this() {
        if (status != 200) {
            statusCode = status
            statusText = statusTextFrom(status)
        }
    }

    constructor(
        status: Int,
        rspHeaders: SortedMap<String, String>,
        rspBody: Optional<String>,
        head: Boolean
    ) : this(status, rspHeaders, rspBody) {
        onlyHead = head
    }

    constructor(
        status: Int,
        rspHeaders: SortedMap<String, String>,
        rspBody: Optional<String>
    ) : this(status) {
        if (rspBody.isPresent) {
            body = rspBody
        }

        rspHeaders.forEach { e -> headers[e.key] = e.value }
        headers["Content-Length"] =
            (if (body.isPresent) body.get().length else 0).toString()
    }

    override fun toString(): String {
        return "${version.version} $statusCode $statusText\r\n" +
                "${headersAsStrings()}\r\n" +
                if (body.isPresent && !onlyHead) body.get() else ""
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
            "\n\t\tResponse Line: ${version.version} $statusCode $statusText\n\t\tHeaders: $headers\n")
        stream.write(rsp.toByteArray())
    }
}