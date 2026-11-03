package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.AudienceCurrentDto
import com.google.gson.JsonObject
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.PlayEventRequestDto
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeviceHealth
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventsApi

/** Converts a domain [PlayEvent] into the request body sent over the wire. */
fun PlayEvent.toRequestDto() = PlayEventRequestDto(
    schemaVersion = PlayEventsApi.versionFor(this),
    eventId = eventId,
    eventType = type.wireName,
    playId = playId,
    campaignId = campaignId,
    contentId = contentId,
    occurredAt = Iso8601.formatUtc(occurredAtMs),
    reason = reason,
    expectedDurationMs = expectedDurationMs,
    advertiser = advertiser,
)

/**
 * Converts a `GET /health` reply into [DeviceHealth].
 *
 * A device that predates `play_events_schema_versions` accepts [PlayEventsApi.BASE_VERSION] only.
 * Entries that are not strings are ignored rather than guessed at.
 */
fun JsonObject.toDeviceHealth(): DeviceHealth {
    val listed = get(PLAY_EVENT_VERSIONS)?.takeIf { it.isJsonArray }?.asJsonArray
    val versions = listed?.mapNotNull { element ->
        element.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString
    }?.toSet() ?: setOf(PlayEventsApi.BASE_VERSION)
    return DeviceHealth(detail = toString(), playEventVersions = versions)
}

private const val PLAY_EVENT_VERSIONS = "play_events_schema_versions"

/**
 * Converts a response body into a domain [AudienceSnapshot].
 *
 * Absent fields stay `null` instead of becoming zero, and a missing status becomes `unknown`.
 */
fun AudienceCurrentDto.toDomain() = AudienceSnapshot(
    measurementStatus = measurementStatus ?: "unknown",
    observedAt = observedAt,
    peopleNow = current?.peopleNow,
    lookingNow = current?.lookingNow,
    gender = current?.demographicsNow?.gender.orEmpty(),
    age = current?.demographicsNow?.age.orEmpty(),
    viewersTotal = session?.viewersTotal,
    avgDwellMs = session?.avgDwellMs,
    reachTotal = session?.reachTotal,
    otsTotal = session?.otsTotal,
    frequency = session?.frequency,
    verifiedReachTotal = session?.verifiedReachTotal,
    staffExcludedTotal = session?.staffExcludedTotal,
    activeCampaignId = activeCampaignId,
)
