package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * Outcome of one call to the device, modelled as a value instead of exceptions so that callers
 * must handle every failure kind.
 *
 * @param T type of the payload returned on success.
 */
sealed interface ApiResult<out T> {

    /**
     * The device answered with a 2xx status.
     *
     * @property value the decoded response payload.
     */
    data class Success<T>(val value: T) : ApiResult<T>

    /**
     * The device answered with a non-2xx status.
     *
     * @property code HTTP status code.
     * @property error short machine-readable error name from the response body, if present.
     * @property message human-readable explanation from the response body, if present.
     * @property retryAfterMs how long the device asked the caller to wait before retrying, if it said.
     */
    data class HttpError(
        val code: Int,
        val error: String?,
        val message: String?,
        val retryAfterMs: Long?,
    ) : ApiResult<Nothing>

    /**
     * No usable HTTP response: the device was unreachable, the request timed out, or the body
     * could not be read.
     *
     * @property message description of what went wrong.
     */
    data class NetworkError(val message: String) : ApiResult<Nothing>
}
