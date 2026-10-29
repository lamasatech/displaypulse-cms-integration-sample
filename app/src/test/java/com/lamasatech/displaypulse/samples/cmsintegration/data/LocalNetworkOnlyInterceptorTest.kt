package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.google.gson.Gson
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsApiFactory
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.LocalNetworkOnlyInterceptor
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.InMemorySettingsStore
import java.io.IOException
import java.net.InetAddress
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests that requests go out only on connections to local addresses. */
class LocalNetworkOnlyInterceptorTest {

    private val interceptor = LocalNetworkOnlyInterceptor()
    private val server = MockWebServer()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `a connection to a public address is refused before the request is sent`() {
        val chain = RecordingChain(RoutedConnection("203.0.113.7"))

        val error = assertThrows(IOException::class.java) { interceptor.intercept(chain) }

        assertFalse(chain.proceeded)
        assertTrue(error.message!!.contains("203.0.113.7"))
    }

    @Test
    fun `a connection to a private address is passed on (control)`() {
        val chain = RecordingChain(RoutedConnection("192.168.1.20"))

        interceptor.intercept(chain)

        assertTrue(chain.proceeded)
    }

    @Test
    fun `a request with no known connection is refused`() {
        val chain = RecordingChain(connection = null)

        assertThrows(IOException::class.java) { interceptor.intercept(chain) }
        assertFalse(chain.proceeded)
    }

    @Test
    fun `the API client still reaches a device on a loopback address`() = runTest {
        server.enqueue(MockResponse().setBody("""{"status":"ok"}"""))
        server.start(InetAddress.getByName("127.0.0.1"), 0)
        val settings = InMemorySettingsStore(ConnectionSettings("127.0.0.1", server.port, "t"))

        val response = CmsApiFactory.create(settings, Gson()).health()

        assertEquals(200, response.code())
        assertEquals(1, server.requestCount)
    }
}
