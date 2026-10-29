package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * Everything the app knows about playback at one moment.
 *
 * @property campaign the campaign new plays are started for, if one has been created.
 * @property activePlay the play the device has confirmed as started, if any.
 * @property pending the one event that was created but not yet acknowledged by the device. It is
 * kept, and re-sent with the same event id, until the device accepts or permanently rejects it.
 */
data class PlaybackState(
    val campaign: Campaign? = null,
    val activePlay: ActivePlay? = null,
    val pending: PlayEvent? = null,
) {
    /** `true` when no play is open and no event is waiting, so the campaign may be replaced. */
    val canChangeCampaign: Boolean get() = activePlay == null && pending == null

    /** `true` when a campaign exists and nothing is open or waiting, so a new play may start. */
    val canStart: Boolean get() = campaign != null && activePlay == null && pending == null

    /** `true` when a confirmed play is open and no event is waiting, so it may be closed. */
    val canEnd: Boolean get() = activePlay != null && pending == null

    /**
     * `true` when no play is open and no event is waiting, so the device address may change. All of a
     * play's events must reach the device that started it.
     */
    val canChangeDevice: Boolean get() = activePlay == null && pending == null
}
