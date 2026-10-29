package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Refuses to send a request unless the connection goes to a local address (see [LocalAddress]).
 *
 * The device is reached over plain HTTP with the access token in a header, so a mistyped host or a
 * host name that resolves to a public address must never receive the request. Android's network
 * security config can only allow cleartext per exact host name, not per address range, so the
 * check is made here, on the address actually connected to, before any byte of the request is sent.
 * Install it as a network interceptor, where the connection is known.
 */
class LocalNetworkOnlyInterceptor : Interceptor {

    /** @throws IOException if the connection goes to a non-local address; OkHttp reports it as a failed call. */
    override fun intercept(chain: Interceptor.Chain): Response {
        val address = chain.connection()?.route()?.socketAddress?.address
            ?: throw IOException("Connection address unknown; request not sent")
        if (!LocalAddress.isLocal(address)) {
            throw IOException("${address.hostAddress} is not a local network address; request not sent")
        }
        return chain.proceed(chain.request())
    }
}
