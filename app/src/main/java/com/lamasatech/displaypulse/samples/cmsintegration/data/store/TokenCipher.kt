package com.lamasatech.displaypulse.samples.cmsintegration.data.store

/** Encrypts the site token before it is stored, so it never sits on disk in plain text. */
interface TokenCipher {

    /**
     * @param token the site token.
     * @return the encrypted token, as text that can be stored, or `null` when the device's key store
     * cannot encrypt it.
     */
    fun encrypt(token: String): String?

    /**
     * @param stored a value returned by [encrypt].
     * @return the token, or `null` when it cannot be decrypted, for example because the key was
     * removed or the device's key store failed.
     */
    fun decrypt(stored: String): String?
}
