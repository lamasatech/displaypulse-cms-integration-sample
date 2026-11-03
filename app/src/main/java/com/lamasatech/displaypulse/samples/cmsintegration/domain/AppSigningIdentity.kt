package com.lamasatech.displaypulse.samples.cmsintegration.domain

/**
 * This app's identity as the DisplayPulse app checks it before sending the audience broadcast.
 * An administrator registers both values on the CMS connection.
 */
interface AppSigningIdentity {

    /** The application id of this app. */
    val packageName: String

    /**
     * @return the SHA-256 digest of this app's signing certificate as 64 lowercase hexadecimal
     * characters, or `null` if it cannot be read or the app has more than one signer, which the
     * DisplayPulse app does not accept.
     */
    fun signingCertificateSha256(): String?
}
