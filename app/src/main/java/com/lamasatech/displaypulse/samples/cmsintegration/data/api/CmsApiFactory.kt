package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import com.google.gson.Gson
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import java.net.Proxy
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Builds the [CmsApi] implementation. Kept separate from dependency injection so tests can use it directly. */
object CmsApiFactory {
    // Retrofit requires a base URL; the interceptor replaces its host and port on every request.
    private const val PLACEHOLDER_BASE_URL = "http://localhost/"

    /**
     * Creates a [CmsApi] that always talks to the address currently stored in [settingsStore].
     *
     * @param settingsStore source of host, port and token, read on every request.
     * @param gson converter used for request and response bodies.
     * @param logLevel how much of each request and response is written to Logcat (tag `OkHttp`).
     * @return a ready-to-use API client with short timeouts suited to a local network. It connects
     * directly, never through a proxy, and only to this device (127.0.0.1).
     */
    fun create(
        settingsStore: ConnectionSettingsStore,
        gson: Gson,
        logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE,
    ): CmsApi {
        // Added after the endpoint interceptor, so the log shows the real address the request went to.
        val logging = HttpLoggingInterceptor().apply {
            level = logLevel
            redactHeader("Authorization")
        }
        val client = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            // A system proxy would receive the token in plain text and hide the device's real address.
            .proxy(Proxy.NO_PROXY)
            .addInterceptor(CmsEndpointInterceptor(settingsStore))
            .addInterceptor(logging)
            .addNetworkInterceptor(SameDeviceOnlyInterceptor())
            .build()
        return Retrofit.Builder()
            .baseUrl(PLACEHOLDER_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CmsApi::class.java)
    }
}
