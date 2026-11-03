package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Request body for reporting a play event.
 *
 * Null optionals are left out of the JSON entirely, because the receiver rejects fields it does
 * not expect rather than ignoring them.
 *
 * @property schemaVersion version of this body format, from
 * [com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventsApi.versionFor].
 * @property eventId unique id of the event; stays the same on retries.
 * @property eventType lifecycle stage: `started`, `completed`, `failed` or `interrupted`.
 * @property playId id of the play the event belongs to.
 * @property campaignId campaign id; sent with `started` events only.
 * @property contentId content id; sent with `started` events only.
 * @property occurredAt ISO-8601 UTC time of the event.
 * @property reason explanation; sent with `failed` and `interrupted` events only.
 * @property expectedDurationMs expected content duration in milliseconds; sent with `started`
 * events only, and only when known.
 * @property advertiser the brand behind the campaign; sent with `started` events in version 1.2 only.
 */
data class PlayEventRequestDto(
    @SerializedName("schema_version") val schemaVersion: String,
    @SerializedName("event_id") val eventId: String,
    @SerializedName("event_type") val eventType: String,
    @SerializedName("play_id") val playId: String,
    @SerializedName("campaign_id") val campaignId: String?,
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("occurred_at") val occurredAt: String,
    @SerializedName("reason") val reason: String?,
    @SerializedName("expected_duration_ms") val expectedDurationMs: Long?,
    @SerializedName("advertiser") val advertiser: String?,
)
