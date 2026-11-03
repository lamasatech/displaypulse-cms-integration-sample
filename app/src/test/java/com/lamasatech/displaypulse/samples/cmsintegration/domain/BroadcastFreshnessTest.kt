package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests when the broadcast counts as stopped, which is when the app falls back to HTTP. */
class BroadcastFreshnessTest {

    private fun receivedAt(elapsedMs: Long) = BroadcastFeedState(receivedAtElapsedMs = elapsedMs, received = 1)

    @Test
    fun `no broadcast yet is not fresh`() {
        assertFalse(BroadcastFreshness.isFresh(BroadcastFeedState(), nowElapsedMs = 10_000))
    }

    @Test
    fun `fresh up to the maximum age, stopped just after it`() {
        val feed = receivedAt(10_000)
        assertTrue(BroadcastFreshness.isFresh(feed, 10_000))
        assertTrue(BroadcastFreshness.isFresh(feed, 10_000 + BroadcastFreshness.MAX_AGE_MS))
        assertFalse(BroadcastFreshness.isFresh(feed, 10_001 + BroadcastFreshness.MAX_AGE_MS))
    }

    @Test
    fun `a receive time in the future is not trusted`() {
        assertFalse(BroadcastFreshness.isFresh(receivedAt(10_000), nowElapsedMs = 9_000))
    }
}
