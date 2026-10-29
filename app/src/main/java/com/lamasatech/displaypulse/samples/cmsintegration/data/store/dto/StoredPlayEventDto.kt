package com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto

import com.google.gson.annotations.SerializedName

/**
 * On-disk form of a play event that has not been acknowledged yet. Its ids are kept exactly, so
 * the event is resent as the same event after a restart.
 *
 * @property eventId id of the event.
 * @property type the event type's wire name, for example `started`.
 * @property playId id of the play the event belongs to.
 * @property campaignId campaign id; set for `started` events only.
 * @property contentId content id; set for `started` events only.
 * @property occurredAtMs when the event happened, in Unix time milliseconds.
 * @property reason explanation; set for `failed` and `interrupted` events only.
 */
data class StoredPlayEventDto(
    @SerializedName("event_id") val eventId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("play_id") val playId: String?,
    @SerializedName("campaign_id") val campaignId: String?,
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("occurred_at_ms") val occurredAtMs: Long?,
    @SerializedName("reason") val reason: String?,
)
