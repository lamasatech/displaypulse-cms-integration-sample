package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests busy-reply handling: same event id, advertised delay, bounded wait and retry budget. */
@OptIn(ExperimentalCoroutinesApi::class)
class PlayEventSenderTest {

    private val event = PlayEventFactory(FixedClock(), SequentialIds())
        .started(Campaign("camp-1", "Demo", "content-1"))
    private val accepted = ApiResult.Success(PlayReceipt(receiptId = 7, duplicate = false))

    private fun limited(retryAfterMs: Long?) = ApiResult.HttpError(429, "rate_limited", null, retryAfterMs)

    @Test
    fun `429 is retried with the same event id after the advertised delay`() = runTest {
        val repo = ScriptedCmsRepository(ArrayDeque(listOf(limited(2_500), accepted)))
        val result = PlayEventSender(repo).send(event)
        assertEquals(accepted, result)
        assertEquals(listOf(event.eventId, event.eventId), repo.sentEventIds)
        assertEquals(2_500, currentTime)
    }

    @Test
    fun `retry delay is bounded`() = runTest {
        val repo = ScriptedCmsRepository(ArrayDeque(listOf(limited(10 * 60_000), accepted)))
        PlayEventSender(repo).send(event)
        assertEquals(30_000, currentTime)
    }

    @Test
    fun `gives up after the retry budget and returns the 429`() = runTest {
        val repo = ScriptedCmsRepository(ArrayDeque(List(10) { limited(1_000) }))
        val result = PlayEventSender(repo).send(event)
        assertTrue(result is ApiResult.HttpError && result.code == 429)
        assertEquals(4, repo.sentEventIds.size) // first attempt + 3 retries
    }

    @Test
    fun `other errors are returned without retry`() = runTest {
        val repo = ScriptedCmsRepository(ArrayDeque(listOf(ApiResult.HttpError(409, "event_id_conflict", null, null))))
        PlayEventSender(repo).send(event)
        assertEquals(1, repo.sentEventIds.size)
    }
}
