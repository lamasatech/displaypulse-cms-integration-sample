package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Points every request at the configured device and adds the access token.
 *
 * The address is entered by the user and can change while the app runs, so it is applied per
 * request instead of being fixed in the Retrofit base URL.
 *
 * @property settingsStore where the current connection settings are read from.
 */
class CmsEndpointInterceptor(private val settingsStore: ConnectionSettingsStore) : Interceptor {

    /**
     * Rewrites scheme, host and port from the current settings and sets the `Authorization` header
     * when a token is configured.
     *
     * @throws IOException if no valid host is configured or the stored token cannot be sent in a
     * header; OkHttp reports it as a failed call. Any other exception thrown here would crash the
     * app on OkHttp's dispatcher thread.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = settingsStore.load()
        if (!settings.isConfigured) throw IOException("Device host is not configured")
        val url = try {
            chain.request().url.newBuilder()
                .scheme("http") // devices on the local network are reached over plain HTTP
                .host(settings.host)
                .port(settings.port)
                .build()
        } catch (e: IllegalArgumentException) {
            throw IOException("Invalid device host: ${settings.host}", e)
        }
        val request = chain.request().newBuilder().url(url)
        if (settings.token.isNotBlank()) {
            // OkHttp throws IllegalArgumentException for such a header, which would crash the app.
            if (!ConnectionSettings.isValidToken(settings.token)) {
                throw IOException("Access token contains characters that cannot be sent; enter it again")
            }
            request.header("Authorization", "Bearer ${settings.token}")
        }
        return chain.proceed(request.build())
    }
}
