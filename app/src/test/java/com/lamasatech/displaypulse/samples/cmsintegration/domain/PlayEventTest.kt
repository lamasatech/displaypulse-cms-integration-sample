package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ContentDuration
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

/** Tests the rules for building play events and the ids [PlayEventFactory] assigns. */
class PlayEventTest {

    private val campaign = Campaign(id = "camp-1", name = "Demo", contentId = "content-1")
    private val factory = PlayEventFactory(FixedClock(), SequentialIds())

    @Test
    fun `started carries the campaign and content ids`() {
        val event = factory.started(campaign, null)
        assertEquals("camp-1", event.campaignId)
        assertEquals("content-1", event.contentId)
        assertNull(event.reason)
    }

    @Test
    fun `every start is a new play with a new event id`() {
        val first = factory.started(campaign, null)
        val second = factory.started(campaign, null)
        assert(first.playId != second.playId) { "adjacent repeats must be distinct plays" }
        assert(first.eventId != second.eventId)
    }

    @Test
    fun `terminal reuses the play id but gets its own event id`() {
        val started = factory.started(campaign, null)
        val play = ActivePlay(started.playId, "camp-1", "content-1", started.occurredAtMs)
        val ended = factory.terminal(play, PlayEventType.COMPLETED, null)
        assertEquals(started.playId, ended.playId)
        assert(ended.eventId != started.eventId)
        assertNull(ended.campaignId)
    }

    @Test
    fun `failed and interrupted require a reason`() {
        val play = ActivePlay("p", "c", "x", 0)
        assertThrows(IllegalArgumentException::class.java) { factory.terminal(play, PlayEventType.FAILED, null) }
        assertThrows(IllegalArgumentException::class.java) { factory.terminal(play, PlayEventType.INTERRUPTED, null) }
    }

    @Test
    fun `completed rejects a reason`() {
        val play = ActivePlay("p", "c", "x", 0)
        assertThrows(IllegalArgumentException::class.java) { factory.terminal(play, PlayEventType.COMPLETED, "why") }
    }

    @Test
    fun `terminal factory refuses the started type`() {
        val play = ActivePlay("p", "c", "x", 0)
        assertThrows(IllegalArgumentException::class.java) { factory.terminal(play, PlayEventType.STARTED, null) }
    }

    @Test
    fun `started without campaign is invalid`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlayEvent("e", PlayEventType.STARTED, "p", null, "c", 0, null)
        }
    }

    @Test
    fun `started carries a duration within the device range, inclusive`() {
        assertEquals(5_000L, factory.started(campaign, 5_000L).expectedDurationMs)
        assertEquals(7_200_000L, factory.started(campaign, 7_200_000L).expectedDurationMs)
        assertNull(factory.started(campaign, null).expectedDurationMs)
    }

    @Test
    fun `a duration outside the device range is refused, not clamped`() {
        listOf(0L, 4_999L, 7_200_001L, -1L).forEach { duration ->
            assertThrows("$duration", IllegalArgumentException::class.java) { factory.started(campaign, duration) }
        }
    }

    @Test
    fun `a closing event cannot carry a duration`() {
        assertThrows(IllegalArgumentException::class.java) {
            PlayEvent("e", PlayEventType.COMPLETED, "p", null, null, 0, null, expectedDurationMs = 60_000L)
        }
        // Control: the same event without a duration is valid.
        PlayEvent("e", PlayEventType.COMPLETED, "p", null, null, 0, null)
    }

    @Test
    fun `typed seconds become milliseconds only for whole seconds in range`() {
        assertEquals(600_000L, ContentDuration.fromSecondsText(" 600 "))
        assertEquals(5_000L, ContentDuration.fromSecondsText("5"))
        assertEquals(7_200_000L, ContentDuration.fromSecondsText("7200"))
        listOf("4", "7201", "0", "-5", "1.5", "abc", "", "99999999999999999999").forEach { text ->
            assertNull(text, ContentDuration.fromSecondsText(text))
        }
    }
}
