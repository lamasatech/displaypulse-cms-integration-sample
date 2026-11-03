package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import com.lamasatech.displaypulse.samples.cmsintegration.domain.AppSigningIdentity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Reads this app's package name and signing certificate from the package manager, which is the
 * same source the DisplayPulse app checks.
 *
 * @property context the application context.
 */
class PackageManagerSigningIdentity @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AppSigningIdentity {

    override val packageName: String get() = context.packageName

    override fun signingCertificateSha256(): String? {
        val signer = try {
            currentSigners()?.singleOrNull()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        return signer?.let { CertificateDigest.sha256Hex(it.toByteArray()) }
    }

    /** @return the certificates the APK is signed with now, ignoring any rotation history. */
    private fun currentSigners(): Array<Signature>? {
        val manager = context.packageManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> manager.getPackageInfo(
                packageName,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            ).signingInfo?.apkContentsSigners
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> manager
                .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners
            else -> legacySignatures(manager)
        }
    }

    @Suppress("DEPRECATION") // GET_SIGNATURES is the only option before Android 9.
    private fun legacySignatures(manager: PackageManager): Array<Signature>? {
        val info: PackageInfo = manager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
        return info.signatures
    }
}
