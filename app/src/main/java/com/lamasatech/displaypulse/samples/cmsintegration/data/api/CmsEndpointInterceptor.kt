package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Points every request at the configured device and adds the site token.
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
     * @throws IOException if the stored host or port is not one the sample accepts, or the stored
     * token cannot be sent in a header; OkHttp reports it as a failed call. Any other exception
     * thrown here would crash the app on OkHttp's dispatcher thread.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val settings = settingsStore.load()
        // Settings are checked when saved; this also covers a value stored by an earlier version.
        if (!ConnectionSettings.isAllowedHost(settings.host)) throw IOException(ConnectionSettings.HOST_REQUIREMENT)
        if (settings.port !in ConnectionSettings.VALID_PORTS) throw IOException("Device port is not configured")
        val url = chain.request().url.newBuilder()
            .scheme("http") // the DisplayPulse app's local API on this device is plain HTTP
            .host(settings.host)
            .port(settings.port)
            .build()
        val request = chain.request().newBuilder().url(url)
        if (settings.token.isNotBlank()) {
            // OkHttp throws IllegalArgumentException for such a header, which would crash the app.
            if (!ConnectionSettings.isValidToken(settings.token)) {
                throw IOException("Site token contains characters that cannot be sent; enter it again")
            }
            request.header("Authorization", "Bearer ${settings.token}")
        }
        return chain.proceed(request.build())
    }
}
