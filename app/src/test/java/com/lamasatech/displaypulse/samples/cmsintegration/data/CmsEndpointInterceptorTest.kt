package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsEndpointInterceptor
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.InMemorySettingsStore
import java.io.IOException
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

/**
 * Tests that a stored token which cannot be sent fails the call as an [IOException]. Any other
 * exception from an interceptor is rethrown by OkHttp on its dispatcher thread and crashes the app;
 * a synchronous call surfaces exactly what the interceptor threw.
 */
class CmsEndpointInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var settings: InMemorySettingsStore
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        settings = InMemorySettingsStore(ConnectionSettings(server.hostName, server.port, "good-token"))
        client = OkHttpClient.Builder().addInterceptor(CmsEndpointInterceptor(settings)).build()
    }

    @After
    fun tearDown() = server.shutdown()

    private fun call() = client.newCall(Request.Builder().url("http://localhost/health").build()).execute()

    @Test
    fun `a stored token that cannot be sent fails as an IOException without reaching the device`() {
        settings.settings = settings.settings.copy(token = "dpcms_abc def")

        assertThrows(IOException::class.java) { call() }
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a valid token is sent as a bearer header (control)`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        call().close()

        assertEquals("Bearer good-token", server.takeRequest().getHeader("Authorization"))
    }
}
