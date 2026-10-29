package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests which access tokens can be sent in an HTTP header. */
class ConnectionSettingsTest {

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
