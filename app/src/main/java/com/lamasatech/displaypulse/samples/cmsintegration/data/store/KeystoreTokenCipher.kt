package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [TokenCipher] using AES-GCM with a key held in the Android Keystore. The key never leaves the
 * Keystore, so a copy of the app's files is not enough to read the token.
 *
 * The stored form is Base64 of the 12-byte IV followed by the ciphertext and its tag.
 *
 * Keystore failures are reported as `null`, never thrown: on some devices the key store throws
 * [ProviderException] or [IOException], and a crash would leave the user unable to start the app.
 */
@Singleton
class KeystoreTokenCipher @Inject constructor() : TokenCipher {

    override fun encrypt(token: String): String? = keystoreCall {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val sealed = cipher.iv + cipher.doFinal(token.toByteArray(Charsets.UTF_8))
        Base64.encodeToString(sealed, Base64.NO_WRAP)
    }

    override fun decrypt(stored: String): String? = keystoreCall {
        val sealed = Base64.decode(stored, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, sealed, 0, IV_BYTES))
        String(cipher.doFinal(sealed, IV_BYTES, sealed.size - IV_BYTES), Charsets.UTF_8)
    }

    /** Runs [operation] and turns every failure the key store and cipher can raise into `null`. */
    private fun keystoreCall(operation: () -> String): String? = try {
        operation()
    } catch (_: GeneralSecurityException) {
        null
    } catch (_: ProviderException) {
        null
    } catch (_: IOException) {
        null
    } catch (_: IllegalArgumentException) {
        null // damaged Base64, or too short to hold an IV
    }

    @Synchronized
    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(KEY_BITS)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "cms_access_token"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_BITS = 256
        const val TAG_BITS = 128
        const val IV_BYTES = 12
    }
}
