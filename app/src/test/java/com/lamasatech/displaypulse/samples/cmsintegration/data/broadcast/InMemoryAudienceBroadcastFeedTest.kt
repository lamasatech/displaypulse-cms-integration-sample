package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import com.google.gson.Gson
import com.lamasatech.displaypulse.samples.cmsintegration.domain.FixedClock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests how the feed records readable and unreadable broadcasts. */
class InMemoryAudienceBroadcastFeedTest {

    private val clock = FixedClock()
    private val feed = InMemoryAudienceBroadcastFeed(AudienceUpdateParser(Gson()), clock)

    private fun payload(peopleNow: Int) = """{"measurement_status":"measured","current":{"people_now":$peopleNow}}"""

    @Test
    fun `starts empty`() {
        val state = feed.state.value
        assertNull(state.latest)
        assertNull(state.receivedAtElapsedMs)
        assertEquals(0, state.received)
        assertEquals(0, state.rejected)
    }

    @Test
    fun `each readable broadcast replaces the latest figures and is counted`() {
        feed.accept(payload(3))
        clock.elapsedMs += 1_000
        feed.accept(payload(5))

        val state = feed.state.value
        assertEquals(5, state.latest?.peopleNow)
        assertEquals(clock.elapsedMs, state.receivedAtElapsedMs)
        assertEquals(2, state.received)
        assertEquals(0, state.rejected)
    }

    @Test
    fun `an unreadable broadcast is counted and keeps the last good figures`() {
        feed.accept(payload(3))
        val receivedAt = clock.elapsedMs
        clock.elapsedMs += 1_000

        feed.accept("{broken")
        feed.accept(null)

        val state = feed.state.value
        assertEquals(3, state.latest?.peopleNow)
        assertEquals(receivedAt, state.receivedAtElapsedMs)
        assertEquals(1, state.received)
        assertEquals(2, state.rejected)
    }
}
