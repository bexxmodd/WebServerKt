package requestlineTests

import bexmod.http.Method
import org.junit.Test
import org.junit.Assert.assertEquals

class MethodTests {

    @Test
    fun createMethodFromStringTest() {
        val actual = Method.from("GET")
        assertEquals(Method.GET, actual)
    }

    @Test
    fun wrongMethodTest() {
        val actual = Method.from("NOTAMETHOD")
        assertEquals(Method.UNINITIALIZED, actual)
    }
}