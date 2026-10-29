package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests which hosts the sample connects to and which site tokens can be sent in an HTTP header. */
class ConnectionSettingsTest {

    @Test
    fun `only this device is an allowed host`() {
        assertTrue(ConnectionSettings.isAllowedHost("127.0.0.1"))
        assertTrue(ConnectionSettings.isAllowedHost("localhost"))
    }

    @Test
    fun `other devices, other loopback addresses and malformed hosts are refused`() {
        for (host in listOf(
            "", "192.168.1.10", "10.0.0.5", "example.com", "127.0.0.2", "::1", "http://127.0.0.1",
            "127.0.0.1:8400", "a\$b;c|d<e>", "a".repeat(300),
        )) {
            assertFalse(host, ConnectionSettings.isAllowedHost(host))
        }
    }

    @Test
    fun `a host is compared without surrounding spaces and in lower case`() {
        assertEquals("localhost", ConnectionSettings.normalizeHost("  LocalHost "))
        assertEquals("127.0.0.1", ConnectionSettings.normalizeHost(" 127.0.0.1\t"))
        assertFalse(ConnectionSettings.isAllowedHost("LocalHost")) // only the normalized form is allowed
    }

    @Test
    fun `a token in the issued format and a blank token are valid`() {
        assertTrue(ConnectionSettings.isValidToken("dpcms_Ex4mple-Token_0~a/b+c="))
        assertTrue(ConnectionSettings.isValidToken(""))
    }

    @Test
    fun `characters picked up by copying from a document are refused`() {
        assertFalse(ConnectionSettings.isValidToken("dpcms_abc def")) // non-breaking space
        assertFalse(ConnectionSettings.isValidToken("dpcms_abc–def")) // en dash
        assertFalse(ConnectionSettings.isValidToken("dpcms_ábc"))
        assertFalse(ConnectionSettings.isValidToken("dpcms_abc def"))
        assertFalse(ConnectionSettings.isValidToken("dpcms_abc\tdef"))
    }
}
