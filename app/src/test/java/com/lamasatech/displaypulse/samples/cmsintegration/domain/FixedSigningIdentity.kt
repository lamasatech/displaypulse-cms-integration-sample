package com.lamasatech.displaypulse.samples.cmsintegration.domain

/** Test [AppSigningIdentity] with fixed values. */
class FixedSigningIdentity(
    override val packageName: String = "com.example.player",
    private val certificateSha256: String? = "ab".repeat(32),
) : AppSigningIdentity {
    override fun signingCertificateSha256() = certificateSha256
}
