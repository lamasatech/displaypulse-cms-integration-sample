package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto.StoredActivePlayDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto.StoredCampaignDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto.StoredPlayEventDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto.StoredPlaybackDto
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import javax.inject.Inject

/**
 * Converts [PlaybackState] to and from the JSON record kept on disk.
 *
 * Reading goes through the domain constructors, so a record that breaks a rule the app relies on
 * (a missing id, a reason on a `started` event, a closing event with no open play) is refused
 * instead of producing a state the app could never get out of.
 *
 * @property gson the JSON converter.
 */
class PlaybackStateCodec @Inject constructor(private val gson: Gson) {

    /**
     * @param state the state to store.
     * @return the JSON record.
     */
    fun encode(state: PlaybackState): String = gson.toJson(
        StoredPlaybackDto(
            version = VERSION,
            campaign = state.campaign?.let { StoredCampaignDto(it.id, it.name, it.contentId) },
            activePlay = state.activePlay?.let {
                StoredActivePlayDto(it.playId, it.campaignId, it.contentId, it.startedAtMs)
            },
            pending = state.pending?.let {
                StoredPlayEventDto(
                    it.eventId, it.type.wireName, it.playId, it.campaignId, it.contentId, it.occurredAtMs, it.reason,
                )
            },
        )
    )

    /**
     * @param json a record written by [encode].
     * @return the stored state, or `null` when the record is damaged, has another [VERSION] or breaks
     * a playback rule.
     */
    fun decode(json: String): PlaybackState? = try {
        gson.fromJson(json, StoredPlaybackDto::class.java)?.takeIf { it.version == VERSION }?.toDomain()
    } catch (_: JsonParseException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }

    private fun StoredPlaybackDto.toDomain(): PlaybackState {
        val state = PlaybackState(
            campaign = campaign?.let { Campaign(required(it.id), it.name.orEmpty(), required(it.contentId)) },
            activePlay = activePlay?.let {
                ActivePlay(required(it.playId), required(it.campaignId), required(it.contentId), required(it.startedAtMs))
            },
            pending = pending?.toDomain(),
        )
        val pending = state.pending
        if (pending != null) {
            // The rules PlaybackReducer follows: a start is pending only with no open play, and a
            // closing event only for the open play.
            val open = state.activePlay
            require(if (pending.type.isTerminal) open?.playId == pending.playId else open == null) {
                "pending ${pending.type.wireName} event does not match the open play"
            }
        }
        return state
    }

    private fun StoredPlayEventDto.toDomain() = PlayEvent(
        eventId = required(eventId),
        type = requireNotNull(PlayEventType.entries.firstOrNull { it.wireName == type }) { "unknown event type $type" },
        playId = required(playId),
        campaignId = campaignId,
        contentId = contentId,
        occurredAtMs = required(occurredAtMs),
        reason = reason,
    )

    private fun <T : Any> required(value: T?): T = requireNotNull(value) { "a required field is missing" }

    private companion object {
        /** Format version of the stored record. Bump it when the record changes incompatibly. */
        const val VERSION = 1
    }
}
