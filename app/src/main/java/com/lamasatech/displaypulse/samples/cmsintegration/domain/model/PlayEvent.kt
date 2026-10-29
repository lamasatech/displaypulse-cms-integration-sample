package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * One lifecycle event of a single play.
 *
 * [eventId] identifies the event and must stay the same on every retry; [playId] identifies the
 * play and must be new for each distinct play, including back-to-back repeats of the same content.
 *
 * The constructor enforces the same rules the device applies, so a malformed event fails here
 * instead of coming back as an error response.
 *
 * @property eventId unique id of this event; reuse it when retrying the same event.
 * @property type lifecycle stage this event reports.
 * @property playId id of the play this event belongs to.
 * @property campaignId campaign the play belongs to; required for [PlayEventType.STARTED] only.
 * @property contentId content item being played; required for [PlayEventType.STARTED] only.
 * @property occurredAtMs when the event happened, in Unix time milliseconds.
 * @property reason why the play failed or was interrupted; required for those types and forbidden otherwise.
 * @throws IllegalArgumentException if a field required for [type] is missing or one that is forbidden is set.
 */
data class PlayEvent(
    val eventId: String,
    val type: PlayEventType,
    val playId: String,
    val campaignId: String?,
    val contentId: String?,
    val occurredAtMs: Long,
    val reason: String?,
) {
    init {
        require(type != PlayEventType.STARTED || (campaignId != null && contentId != null)) {
            "A started event needs campaignId and contentId"
        }
        require(type.requiresReason == (reason != null)) {
            "reason is required for failed/interrupted and forbidden for started/completed"
        }
    }
}
