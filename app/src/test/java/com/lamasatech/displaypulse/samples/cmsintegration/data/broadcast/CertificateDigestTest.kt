package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests the certificate digest format registered on a CMS connection. */
class CertificateDigestTest {

    @Test
    fun `digest is 64 lowercase hex characters without separators`() {
        // SHA-256("abc"), FIPS 180-2 test vector.
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            CertificateDigest.sha256Hex("abc".toByteArray()),
        )
    }
}
