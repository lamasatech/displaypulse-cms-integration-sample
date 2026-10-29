package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeliveryOutcome
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import org.junit.Assert.assertEquals
import org.junit.Test

/** Tests which results are final, which are refused for good, and which are worth retrying. */
class DeliveryOutcomeClassifierTest {

    private val classifier = DeliveryOutcomeClassifier()

    private fun http(code: Int) = classifier.classify(ApiResult.HttpError(code, null, null, null))

    @Test
    fun `success is acknowledged`() {
        assertEquals(
            DeliveryOutcome.ACKNOWLEDGED,
            classifier.classify(ApiResult.Success(PlayReceipt(receiptId = 7, duplicate = false))),
        )
    }

    @Test
    fun `invalid and conflicting requests are rejected for good`() {
        assertEquals(DeliveryOutcome.REJECTED, http(400))
        assertEquals(DeliveryOutcome.REJECTED, http(409))
    }

    @Test
    fun `terminal conflict means the device already ended the play`() {
        val ended = classifier.classify(ApiResult.HttpError(409, "terminal_conflict", null, null))
        assertEquals(DeliveryOutcome.ALREADY_ENDED, ended)
    }

    @Test
    fun `other conflicts stay rejected`() {
        assertEquals(DeliveryOutcome.REJECTED, classifier.classify(ApiResult.HttpError(409, "play_identity_conflict", null, null)))
        assertEquals(DeliveryOutcome.REJECTED, classifier.classify(ApiResult.HttpError(400, "terminal_conflict", null, null)))
    }

    @Test
    fun `bad token, unfinished set-up or plan, and rate limit are retryable`() {
        assertEquals(DeliveryOutcome.RETRYABLE, http(401))
        assertEquals(DeliveryOutcome.RETRYABLE, http(403))
        assertEquals(DeliveryOutcome.RETRYABLE, http(408))
        assertEquals(DeliveryOutcome.RETRYABLE, http(429))
    }

    @Test
    fun `server errors and network failures are retryable`() {
        assertEquals(DeliveryOutcome.RETRYABLE, http(503))
        assertEquals(DeliveryOutcome.RETRYABLE, classifier.classify(ApiResult.NetworkError("down")))
    }
}
