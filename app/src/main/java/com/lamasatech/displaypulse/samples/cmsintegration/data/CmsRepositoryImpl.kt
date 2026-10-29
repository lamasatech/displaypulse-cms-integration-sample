package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsApi
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.ErrorBodyDto
import com.lamasatech.displaypulse.samples.cmsintegration.domain.CmsRepository
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import java.io.IOException
import javax.inject.Inject
import retrofit2.Response

/**
 * [CmsRepository] that talks to the device through [CmsApi].
 *
 * Network, HTTP and parsing failures are converted into [ApiResult] values; nothing is thrown to the caller.
 *
 * @property api the HTTP client.
 * @property gson used to read error bodies.
 */
class CmsRepositoryImpl @Inject constructor(
    private val api: CmsApi,
    private val gson: Gson,
) : CmsRepository {

    override suspend fun health(): ApiResult<String> =
        execute({ api.health() }) { _, body -> body.toString() }

    override suspend fun audienceCurrent(): ApiResult<AudienceSnapshot> =
        execute({ api.audienceCurrent() }) { _, dto -> dto.toDomain() }

    override suspend fun sendPlayEvent(event: PlayEvent): ApiResult<PlayReceipt> =
        execute({ api.postPlayEvent(event.toRequestDto()) }) { response, body ->
            // 202 = newly accepted, 200 = identical retry of an event the device already holds.
            PlayReceipt(receiptId = body.receiptId, duplicate = response.code() == HTTP_OK)
        }

    /**
     * Runs one request and converts its outcome into an [ApiResult].
     *
     * @param request the call to perform.
     * @param transform builds the success value from the response and its decoded body.
     */
    private suspend fun <T, R> execute(
        request: suspend () -> Response<T>,
        transform: (Response<T>, T) -> R,
    ): ApiResult<R> = try {
        val response = request()
        val body = response.body()
        when {
            !response.isSuccessful -> response.toHttpError()
            body == null -> ApiResult.NetworkError("Empty response body (HTTP ${response.code()})")
            else -> ApiResult.Success(transform(response, body))
        }
    } catch (e: IOException) {
        ApiResult.NetworkError(e.message ?: e.javaClass.simpleName)
    } catch (e: JsonParseException) {
        ApiResult.NetworkError("Unreadable response: ${e.message}")
    }

    private fun Response<*>.toHttpError(): ApiResult.HttpError {
        val body = errorBody()?.string()?.let { raw ->
            try {
                gson.fromJson(raw, ErrorBodyDto::class.java)
            } catch (_: JsonParseException) {
                null
            }
        }
        val headerRetryMs = headers()["Retry-After"]?.toLongOrNull()?.let { it * 1_000L }
        return ApiResult.HttpError(
            code = code(),
            error = body?.error,
            message = body?.message ?: message().takeIf { it.isNotBlank() },
            retryAfterMs = body?.retryAfterMs ?: headerRetryMs,
        )
    }

    private companion object {
        const val HTTP_OK = 200
    }
}
