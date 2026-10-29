package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Refuses to send a request unless the connection goes to this device (see [SameDeviceAddress]).
 *
 * The DisplayPulse app is reached over plain HTTP with the site token in a header, so the request
 * must never leave the device: not to the local network, and not to the internet. Android's network
 * security config allows cleartext only to 127.0.0.1 and localhost; this check also covers host
 * names that resolve elsewhere, on the address actually connected to, before any byte of the
 * request is sent. Install it as a network interceptor, where the connection is known.
 */
class SameDeviceOnlyInterceptor : Interceptor {

    /** @throws IOException if the connection goes to another device; OkHttp reports it as a failed call. */
    override fun intercept(chain: Interceptor.Chain): Response {
        val address = chain.connection()?.route()?.socketAddress?.address
            ?: throw IOException("Connection address unknown; request not sent")
        if (!SameDeviceAddress.isSameDevice(address)) {
            throw IOException(
                "${address.hostAddress} is not this device; request not sent. " +
                    "Run the sample on the same device as the DisplayPulse app and use 127.0.0.1",
            )
        }
        return chain.proceed(chain.request())
    }
}
