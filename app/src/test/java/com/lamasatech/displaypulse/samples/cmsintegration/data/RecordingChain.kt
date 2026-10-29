package com.lamasatech.displaypulse.samples.cmsintegration.data

import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response

/**
 * Test [Interceptor.Chain] over a given [connection] that records whether the request was passed on.
 *
 * @property connection the connection the request would go out on.
 * @property proceeded `true` once the interceptor passed the request on.
 */
class RecordingChain(private val connection: Connection?) : Interceptor.Chain {
    private val request = Request.Builder().url("http://device/health").build()
    var proceeded = false

    override fun request(): Request = request

    override fun proceed(request: Request): Response {
        proceeded = true
        return Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(200).message("OK").build()
    }

    override fun connection(): Connection? = connection

    override fun call(): Call = throw UnsupportedOperationException()

    override fun connectTimeoutMillis() = 0

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit) = this

    override fun readTimeoutMillis() = 0

    override fun withReadTimeout(timeout: Int, unit: TimeUnit) = this

    override fun writeTimeoutMillis() = 0

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit) = this
}
