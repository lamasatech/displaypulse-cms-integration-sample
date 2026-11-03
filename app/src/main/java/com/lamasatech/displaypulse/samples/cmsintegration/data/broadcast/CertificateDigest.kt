package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import java.security.MessageDigest

/** Formats a signing certificate the way a CMS connection's recipient field expects it. */
object CertificateDigest {

    /**
     * @param certificate the DER-encoded signing certificate.
     * @return its SHA-256 digest as 64 lowercase hexadecimal characters, without separators.
     */
    fun sha256Hex(certificate: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(certificate)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
}
