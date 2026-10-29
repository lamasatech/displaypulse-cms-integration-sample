package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import javax.inject.Inject

/**
 * Builds [PlayEvent]s with fresh ids and the current time.
 *
 * @property clock supplies the event timestamps.
 * @property ids supplies event and play ids.
 */
class PlayEventFactory @Inject constructor(
    private val clock: Clock,
    private val ids: IdGenerator,
) {

    /**
     * Creates the event that opens a new play.
     *
     * Every call produces a new play id, so adjacent repeats of the same content remain distinct plays.
     *
     * @param campaign the campaign the play belongs to.
     * @param expectedDurationMs how long the content is expected to play, or `null` when unknown.
     * @return a [PlayEventType.STARTED] event.
     * @throws IllegalArgumentException if [expectedDurationMs] is outside the accepted range.
     */
    fun started(campaign: Campaign, expectedDurationMs: Long?): PlayEvent = PlayEvent(
        eventId = ids.newId(),
        type = PlayEventType.STARTED,
        playId = ids.newId(),
        campaignId = campaign.id,
        contentId = campaign.contentId,
        occurredAtMs = clock.nowMs(),
        reason = null,
        expectedDurationMs = expectedDurationMs,
    )

    /**
     * Creates the event that closes [play].
     *
     * @param play the play to close.
     * @param type how it ended; must be a terminal type.
     * @param reason explanation, required for failed and interrupted plays.
     * @return a terminal event that carries the play's id.
     * @throws IllegalArgumentException if [type] is not terminal or [reason] does not fit [type].
     */
    fun terminal(play: ActivePlay, type: PlayEventType, reason: String?): PlayEvent {
        require(type.isTerminal) { "$type is not a terminal event type" }
        return PlayEvent(
            eventId = ids.newId(),
            type = type,
            playId = play.playId,
            campaignId = null,
            contentId = null,
            occurredAtMs = clock.nowMs(),
            reason = reason,
        )
    }
}
