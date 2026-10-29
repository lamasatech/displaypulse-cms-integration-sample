package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Body of every non-2xx response.
 *
 * @property error short machine-readable error name.
 * @property message human-readable explanation.
 * @property retryAfterMs how long to wait before retrying, in milliseconds, when the device asks for a pause.
 */
data class ErrorBodyDto(
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("retry_after_ms") val retryAfterMs: Long?,
)
