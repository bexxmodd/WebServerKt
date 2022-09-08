package httpTests

import bexmod.http.HttpRequest
import bexmod.http.HttpResponse
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpResponseTest {

    @Test
    fun testHttpRspSimple() {
        val actual = HttpResponse().toString()
        val expected = "HTTP/1.1 200 Success\r\n\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testHttpRspWithHeadersOnly() {
        val h = sortedMapOf("Content-type" to "text/html")
        val actual = HttpResponse(200, h, Optional.empty()).toString()
        val expected = "HTTP/1.1 200 Success\r\nContent-Length: 0\r\nContent-type: text/html\r\n\r\n"
        assertEquals(expected, actual)
    }

    @Test
    fun testHttpRspWithHeadersAndBody() {
        val h = sortedMapOf("Content-type" to "text/html")
        val actual = HttpResponse(200, h, Optional.of("Test")).toString()
        val expected = "HTTP/1.1 200 Success\r\nContent-Length: 4\r\nContent-type: text/html\r\n\r\nTest"
        assertEquals(expected, actual)
    }

    @Test
    fun testHttpRspWithLotsHeadersAndBody() {
        val h = sortedMapOf("Content-type" to "text/html", "Modified" to "Dec 24 2022")
        val actual = HttpResponse(200, h, Optional.of("Test12345")).toString()
        val expected = "HTTP/1.1 200 Success\r\nContent-Length: 9\r\nContent-type: text/html\r\nModified: Dec 24 2022\r\n\r\nTest12345"
        assertEquals(expected, actual)
    }

    @Test
    fun testHttpRspReadingFile() {
        val req = HttpRequest("GET /textResources/file1.txt HTTP/1.1")
        val actual = HttpResponse()
    }
}