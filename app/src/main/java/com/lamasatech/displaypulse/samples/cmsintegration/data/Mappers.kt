package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.AudienceCurrentDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.PlayEventRequestDto
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent

/** Converts a domain [PlayEvent] into the request body sent over the wire. */
fun PlayEvent.toRequestDto() = PlayEventRequestDto(
    eventId = eventId,
    eventType = type.wireName,
    playId = playId,
    campaignId = campaignId,
    contentId = contentId,
    occurredAt = Iso8601.formatUtc(occurredAtMs),
    reason = reason,
)

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
