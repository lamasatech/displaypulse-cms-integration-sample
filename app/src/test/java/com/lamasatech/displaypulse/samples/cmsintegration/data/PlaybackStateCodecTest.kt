package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.google.gson.Gson
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PlaybackStateCodec
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Tests the on-disk record of the playback state: exact round trips and refusal of bad records. */
class PlaybackStateCodecTest {

    private val codec = PlaybackStateCodec(Gson())

    private val campaign = Campaign("camp-1", "Demo", "content-1")
    private val play = ActivePlay("play-1", "camp-1", "content-1", startedAtMs = 1_000L)
    private val pendingStart = PlayEvent("evt-1", PlayEventType.STARTED, "play-1", "camp-1", "content-1", 1_000L, null)
    private val pendingFail = PlayEvent("evt-2", PlayEventType.FAILED, "play-1", null, null, 2_000L, "player_error")

    @Test
    fun `a pending start round-trips with the same ids`() {
        val state = PlaybackState(campaign = campaign, pending = pendingStart)

        assertEquals(state, codec.decode(codec.encode(state)))
    }

    @Test
    fun `an open play with a pending closing event round-trips with the same ids`() {
        val state = PlaybackState(campaign = campaign, activePlay = play, pending = pendingFail)

        assertEquals(state, codec.decode(codec.encode(state)))
    }

    @Test
    fun `an empty state round-trips`() {
        assertEquals(PlaybackState(), codec.decode(codec.encode(PlaybackState())))
    }

    @Test
    fun `a record of another version is not read`() {
        val json = codec.encode(PlaybackState(campaign = campaign)).replace("\"version\":1", "\"version\":2")

        assertNull(codec.decode(json))
    }

    @Test
    fun `a damaged record is not read`() {
        assertNull(codec.decode("{\"version\":1,\"pending\":"))
        assertNull(codec.decode("not json"))
    }

    @Test
    fun `a pending event with a missing id is not read`() {
        val json = codec.encode(PlaybackState(campaign = campaign, pending = pendingStart))
            .replace("\"event_id\":\"evt-1\",", "")

        assertNull(codec.decode(json))
    }

    @Test
    fun `a pending event of an unknown type is not read`() {
        val json = codec.encode(PlaybackState(campaign = campaign, pending = pendingStart))
            .replace("\"type\":\"started\"", "\"type\":\"paused\"")

        assertNull(codec.decode(json))
    }

    @Test
    fun `a pending event that breaks the event rules is not read`() {
        // A started event must not carry a reason.
        val json = codec.encode(PlaybackState(campaign = campaign, pending = pendingStart))
            .replace("\"occurred_at_ms\":1000", "\"occurred_at_ms\":1000,\"reason\":\"x\"")

        assertNull(codec.decode(json))
    }

    @Test
    fun `a closing event with no open play is not read`() {
        val json = codec.encode(PlaybackState(campaign = campaign, activePlay = play, pending = pendingFail))
            .replace(Regex("\"active_play\":\\{[^}]*\\},"), "")

        assertNull(codec.decode(json))
    }

    @Test
    fun `a closing event for a different play is not read`() {
        val other = play.copy(playId = "play-2")

        assertNull(codec.decode(codec.encode(PlaybackState(campaign, other, pendingFail))))
    }

    @Test
    fun `a pending start while a play is open is not read`() {
        assertNull(codec.decode(codec.encode(PlaybackState(campaign, play, pendingStart))))
    }
}
