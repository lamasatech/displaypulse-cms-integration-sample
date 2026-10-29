package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Delivers one play event and waits out "busy" replies, always resending the same event id.
 *
 * @property repository the device connection used for sending.
 */
class PlayEventSender @Inject constructor(private val repository: CmsRepository) {

    /**
     * Sends [event]. When the device answers that it is busy (HTTP 429), waits for the delay it
     * asks for (bounded to a sane range) and resends, up to a small retry budget.
     *
     * @param event the event to deliver.
     * @param onRateLimited called before each wait with the delay in milliseconds, so the UI can show it.
     * @return the last result: the acknowledgement, or the final failure once the budget is used up.
     */
    suspend fun send(event: PlayEvent, onRateLimited: (waitMs: Long) -> Unit = {}): ApiResult<PlayReceipt> {
        var rateLimitedAttempts = 0
        while (true) {
            val result = repository.sendPlayEvent(event)
            val rateLimited = result is ApiResult.HttpError && result.code == HTTP_TOO_MANY_REQUESTS
            if (!rateLimited || rateLimitedAttempts >= MAX_RATE_LIMIT_RETRIES) return result
            rateLimitedAttempts++
            val waitMs = ((result as ApiResult.HttpError).retryAfterMs ?: DEFAULT_WAIT_MS)
                .coerceIn(MIN_WAIT_MS, MAX_WAIT_MS)
            onRateLimited(waitMs)
            delay(waitMs)
        }
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429
        const val MAX_RATE_LIMIT_RETRIES = 3
        const val DEFAULT_WAIT_MS = 1_000L
        const val MIN_WAIT_MS = 1_000L
        const val MAX_WAIT_MS = 30_000L
    }
}
