package com.mariofronza.face_collection_app.api

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ApiServiceTest {

    private class MemorySessionStore(override var current: Session?) : SessionStore

    private val user = User(1, "Ana", "ana@example.com", "teacher")
    private val server = MockWebServer()
    private lateinit var store: MemorySessionStore
    private lateinit var api: ApiService

    private fun json(code: Int, body: String) =
        MockResponse.Builder().code(code).addHeader("Content-Type", "application/json").body(body).build()

    private fun sessionJson(token: String, refreshToken: String) =
        """{"user":{"id":1,"name":"Ana","email":"ana@example.com","role":"teacher"},"token":"$token","refreshToken":"$refreshToken"}"""

    @Before
    fun setUp() {
        server.start()
        store = MemorySessionStore(Session(user, "old-token", "refresh-1"))
        api = ApiService.create(server.url("/").toString(), store)
    }

    @After
    fun tearDown() = server.close()

    @Test
    fun `sends the stored token`() = runTest {
        server.enqueue(json(200, "[]"))

        safeCall { api.photos() }

        assertEquals("Bearer old-token", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `refreshes an expired token once and retries the request`() = runTest {
        server.enqueue(json(401, """{"message":"Invalid JWT token"}"""))
        server.enqueue(json(200, sessionJson("new-token", "refresh-2")))
        server.enqueue(json(200, "[]"))

        val result = safeCall { api.photos() }

        assertTrue(result.isSuccess)
        assertEquals("/photos", server.takeRequest().url.encodedPath)
        val refresh = server.takeRequest()
        assertEquals("/refresh-tokens", refresh.url.encodedPath)
        assertTrue(refresh.body!!.utf8().contains("refresh-1"))
        assertEquals("Bearer new-token", server.takeRequest().headers["Authorization"])
        assertEquals("refresh-2", store.current?.refreshToken)
    }

    @Test
    fun `ends the session when the refresh token is rejected`() = runTest {
        server.enqueue(json(401, """{"message":"Invalid JWT token"}"""))
        server.enqueue(json(401, """{"message":"Token not found."}"""))

        val result = safeCall { api.photos() }

        assertEquals(401, (result.exceptionOrNull() as ApiError).status)
        assertNull(store.current)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `gives up when the retried request is still unauthorized`() = runTest {
        server.enqueue(json(401, """{"message":"Invalid JWT token"}"""))
        server.enqueue(json(200, sessionJson("new-token", "refresh-2")))
        server.enqueue(json(401, """{"message":"Invalid JWT token"}"""))

        val result = safeCall { api.photos() }

        assertEquals(401, (result.exceptionOrNull() as ApiError).status)
        assertEquals(3, server.requestCount)
    }

    @Test
    fun `does not refresh a rejected sign in while an old session is stored`() = runTest {
        server.enqueue(json(401, """{"message":"Incorrect email/password combination."}"""))

        val result = safeCall { api.signIn(SessionRequest("ana@example.com", "wrong")) }

        assertEquals("Incorrect email/password combination.", result.exceptionOrNull()?.message)
        assertEquals(1, server.requestCount)
        assertEquals("refresh-1", store.current?.refreshToken)
    }

    @Test
    fun `does not refresh a rejected sign in`() = runTest {
        store.current = null
        server.enqueue(json(401, """{"message":"Incorrect email/password combination."}"""))

        val result = safeCall { api.signIn(SessionRequest("ana@example.com", "wrong")) }

        assertEquals("Incorrect email/password combination.", result.exceptionOrNull()?.message)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `reports an unreachable server without crashing`() = runTest {
        server.close()

        val result = safeCall { api.photos() }

        assertEquals("Falha de conexão com o servidor", result.exceptionOrNull()?.message)
    }
}
