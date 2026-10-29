package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests every allowed and forbidden transition of the playback state machine. */
class PlaybackReducerTest {

    private val reducer = PlaybackReducer(PlayEventFactory(FixedClock(), SequentialIds()))
    private val campaign = Campaign("camp-1", "Demo", "content-1")
    private val ready = PlaybackState(campaign = campaign)

    @Test
    fun `start is acknowledged into an active play`() {
        val pending = reducer.requestStart(ready, null)
        assertFalse(pending.canStart)
        val active = reducer.acknowledged(pending)
        assertNull(active.pending)
        assertEquals("camp-1", active.activePlay?.campaignId)
        assertEquals(pending.pending?.playId, active.activePlay?.playId)
    }

    @Test
    fun `while an event is pending nothing else can be started, ended or switched`() {
        val pending = reducer.requestStart(ready, null)
        assertFalse(pending.canStart)
        assertFalse(pending.canEnd)
        assertFalse(pending.canChangeCampaign)
        assertThrows(IllegalStateException::class.java) { reducer.requestStart(pending, null) }
    }

    @Test
    fun `terminal acknowledgement closes the play`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        val closing = reducer.requestEnd(active, PlayEventType.COMPLETED, null)
        assertEquals(active.activePlay?.playId, closing.pending?.playId)
        val closed = reducer.acknowledged(closing)
        assertNull(closed.activePlay)
        assertTrue(closed.canStart)
    }

    @Test
    fun `rejected start leaves no play on screen`() {
        val rejected = reducer.rejected(reducer.requestStart(ready, null))
        assertNull(rejected.activePlay)
        assertNull(rejected.pending)
        assertTrue(rejected.canStart)
    }

    @Test
    fun `rejected terminal keeps the play open so it can be ended again`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        val rejected = reducer.rejected(reducer.requestEnd(active, PlayEventType.FAILED, "boom"))
        assertEquals(active.activePlay, rejected.activePlay)
        assertTrue(rejected.canEnd)
    }

    @Test
    fun `device-ended play is closed locally so a new play can start`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        val ended = reducer.endedByDevice(reducer.requestEnd(active, PlayEventType.COMPLETED, null))
        assertNull(ended.activePlay)
        assertNull(ended.pending)
        assertTrue(ended.canStart)
    }

    @Test
    fun `cannot start twice, end without a play, or switch campaign mid-play`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        assertThrows(IllegalStateException::class.java) { reducer.requestStart(active, null) }
        assertThrows(IllegalStateException::class.java) {
            reducer.requestEnd(ready, PlayEventType.COMPLETED, null)
        }
        assertThrows(IllegalStateException::class.java) { reducer.withCampaign(active, campaign) }
    }

    @Test
    fun `cannot start without a campaign`() {
        assertThrows(IllegalStateException::class.java) { reducer.requestStart(PlaybackState(), null) }
    }

    @Test
    fun `acknowledging with nothing pending changes nothing`() {
        assertEquals(ready, reducer.acknowledged(ready))
    }

    @Test
    fun `discarding a closing event also closes the play`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        val discarded = reducer.discarded(reducer.requestEnd(active, PlayEventType.COMPLETED, null))
        assertNull(discarded.pending)
        assertNull(discarded.activePlay)
        assertTrue(discarded.canStart)
    }

    @Test
    fun `discarding a start leaves no play open`() {
        val discarded = reducer.discarded(reducer.requestStart(ready, null))
        assertNull(discarded.pending)
        assertNull(discarded.activePlay)
        assertTrue(discarded.canStart)
    }

    @Test
    fun `discarding with nothing pending changes nothing, even with a play open`() {
        val active = reducer.acknowledged(reducer.requestStart(ready, null))
        assertEquals(active, reducer.discarded(active))
    }

    @Test
    fun `the device address may change only when nothing is open or waiting`() {
        assertTrue(ready.canChangeDevice)
        assertFalse(reducer.requestStart(ready, null).canChangeDevice)
        assertFalse(reducer.acknowledged(reducer.requestStart(ready, null)).canChangeDevice)
    }
}
