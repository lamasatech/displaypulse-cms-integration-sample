package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ConnectionSettingsStore] backed by the app's private preferences. The access token is stored
 * encrypted by [tokenCipher]; the host and port are not secret and are stored as they are.
 *
 * The settings are read for every request, so they are kept in memory after the first read instead
 * of being decrypted each time.
 *
 * @property tokenCipher encrypts the token before it is written and decrypts it when read.
 */
@Singleton
class PrefsConnectionSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
    private val tokenCipher: TokenCipher,
) : ConnectionSettingsStore {
    private val prefs = SamplePreferences.open(context)

    // Guarded by this store's lock: requests read the settings on OkHttp threads while the UI saves them.
    private var cached: ConnectionSettings? = null

    @Synchronized
    override fun load(): ConnectionSettings = cached ?: ConnectionSettings(
        host = prefs.getString(KEY_HOST, "").orEmpty(),
        port = prefs.getInt(KEY_PORT, ConnectionSettings.DEFAULT_PORT),
        token = loadToken(),
    ).also { cached = it }

    @Synchronized
    override fun save(settings: ConnectionSettings): Boolean {
        cached = settings
        val encrypted = if (settings.token.isEmpty()) null else tokenCipher.encrypt(settings.token)
        prefs.edit {
            putString(KEY_HOST, settings.host)
            putInt(KEY_PORT, settings.port)
            // Never fall back to plain text: without encryption the token is not stored at all.
            if (encrypted == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, encrypted)
        }
        return settings.token.isEmpty() || encrypted != null
    }

    private fun loadToken(): String {
        val stored = prefs.getString(KEY_TOKEN, null) ?: return ""
        return tokenCipher.decrypt(stored) ?: run {
            // The user enters the token again; the device answers 401 until then.
            Log.w(TAG, "The stored access token could not be decrypted; it must be entered again")
            ""
        }
    }

    private companion object {
        const val TAG = "ConnectionSettings"
        const val KEY_HOST = "host"
        const val KEY_PORT = "port"
        const val KEY_TOKEN = "token_encrypted"
    }
}
