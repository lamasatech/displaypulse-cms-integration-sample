package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeliveryOutcome
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins how the app reacts to every `/v1/play-events` reply the DisplayPulse app (Audience feature)
 * can send, as listed in the play events API reference, so a new reply shows up here instead of as
 * a stuck screen.
 */
class PlayEventReplyContractTest {

    private val classifier = DeliveryOutcomeClassifier()

    private fun reply(code: Int, error: String) =
        classifier.classify(ApiResult.HttpError(code, error, null, null))

    @Test
    fun `replies that mean the event is final and refused`() {
        listOf(
            400 to "bad_request",
            400 to "clock_invalid",
            403 to "tier_not_entitled",
            403 to "connection_identity_unavailable",
            404 to "not_found",
            409 to "event_id_conflict",
            409 to "play_already_started",
            409 to "play_identity_conflict",
            413 to "payload_too_large",
            415 to "unsupported_media_type",
        ).forEach { (code, error) ->
            assertEquals("$code $error", DeliveryOutcome.REJECTED, reply(code, error))
        }
    }

    @Test
    fun `replies worth sending again unchanged`() {
        listOf(
            401 to "unauthorized",
            429 to "too_many_requests",
            503 to "storage_unavailable",
        ).forEach { (code, error) ->
            assertEquals("$code $error", DeliveryOutcome.RETRYABLE, reply(code, error))
        }
    }

    @Test
    fun `only the terminal conflict closes the play`() {
        assertEquals(DeliveryOutcome.ALREADY_ENDED, reply(409, "terminal_conflict"))
    }
}
