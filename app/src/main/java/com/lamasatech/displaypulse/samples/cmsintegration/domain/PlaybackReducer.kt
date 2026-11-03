package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import javax.inject.Inject

/**
 * Pure state transitions for [PlaybackState].
 *
 * Each function returns a new state and never mutates its input. Calling a transition that the
 * current state does not allow is a programming error and throws [IllegalStateException]; callers
 * check the `can…` properties of [PlaybackState] first.
 *
 * @property events creates the events recorded as pending.
 */
class PlaybackReducer @Inject constructor(private val events: PlayEventFactory) {

    /**
     * Selects the campaign for future plays.
     *
     * @param state the current state; must satisfy [PlaybackState.canChangeCampaign].
     * @param campaign the campaign to use.
     * @return the updated state.
     */
    fun withCampaign(state: PlaybackState, campaign: Campaign): PlaybackState {
        check(state.canChangeCampaign) { "Cannot change campaign while a play is open or pending" }
        return state.copy(campaign = campaign)
    }

    /**
     * Records a pending `started` event for a new play.
     *
     * @param state the current state; must satisfy [PlaybackState.canStart].
     * @param expectedDurationMs how long the content is expected to play, or `null` when unknown.
     * @return the state with the new event pending.
     */
    fun requestStart(state: PlaybackState, expectedDurationMs: Long?): PlaybackState {
        check(state.canStart) { "Cannot start a play in this state" }
        return state.copy(pending = events.started(requireNotNull(state.campaign), expectedDurationMs))
    }

    /**
     * Records a pending terminal event for the open play.
     *
     * @param state the current state; must satisfy [PlaybackState.canEnd].
     * @param type how the play ended; must be terminal.
     * @param reason explanation, required for failed and interrupted plays.
     * @return the state with the new event pending.
     */
    fun requestEnd(state: PlaybackState, type: PlayEventType, reason: String?): PlaybackState {
        check(state.canEnd) { "Cannot end a play in this state" }
        return state.copy(pending = events.terminal(requireNotNull(state.activePlay), type, reason))
    }

    /**
     * Applies the device's acceptance of the pending event: a `started` event opens the play,
     * a terminal event closes it. Does nothing when no event is pending.
     *
     * @param state the current state.
     * @return the updated state with no pending event.
     */
    fun acknowledged(state: PlaybackState): PlaybackState {
        val event = state.pending ?: return state
        return if (event.type == PlayEventType.STARTED) {
            state.copy(
                activePlay = ActivePlay(
                    playId = event.playId,
                    campaignId = requireNotNull(event.campaignId),
                    contentId = requireNotNull(event.contentId),
                    startedAtMs = event.occurredAtMs,
                ),
                pending = null,
            )
        } else {
            state.copy(activePlay = null, pending = null)
        }
    }

    /**
     * Drops the pending event after the device refused it for good. An open play stays open so
     * the user can try to end it again with a new event.
     *
     * @param state the current state.
     * @return the state with no pending event.
     */
    fun rejected(state: PlaybackState): PlaybackState = state.copy(pending = null)

    /**
     * Closes the open play after the device reported it had already ended it, so the user is not
     * stuck with a play no event can end.
     *
     * @param state the current state.
     * @return the state with no open play and no pending event.
     */
    fun endedByDevice(state: PlaybackState): PlaybackState = state.copy(activePlay = null, pending = null)

    /**
     * Gives up on the pending event at the user's request, for example when the device keeps
     * answering `503` because play reporting is switched off on it.
     *
     * A discarded `started` event leaves no play open here. A discarded terminal event also closes
     * the play here; the device ends any play it still holds after its open-play timeout. Does
     * nothing when no event is pending.
     *
     * @param state the current state.
     * @return the state with no pending event.
     */
    fun discarded(state: PlaybackState): PlaybackState {
        val event = state.pending ?: return state
        return if (event.type.isTerminal) state.copy(activePlay = null, pending = null) else state.copy(pending = null)
    }
}
