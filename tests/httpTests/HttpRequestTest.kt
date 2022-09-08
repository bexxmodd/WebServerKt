package httpTests

import bexmod.http.HttpRequest
import bexmod.http.Method
import bexmod.http.Version
import org.junit.Assert.assertEquals
import org.junit.Test

class HttpRequestTest {

    @Test
    fun simpleOkRequestTest() {
        val req = HttpRequest("GET /api HTTP/1.1")
        assertEquals(req.method, Method.GET)
        assertEquals(req.version, Version.V1_1)
        assertEquals(req.resource.path, "/api")
    }
}