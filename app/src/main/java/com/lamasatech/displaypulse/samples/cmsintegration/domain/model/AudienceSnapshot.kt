package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * Live measurements reported by the device at one moment.
 *
 * A `null` value means the device did not provide that field to this connection, or it cannot be
 * measured right now. It is never the same as zero, so display code must not render it as 0.
 *
 * @property measurementStatus freshness of the data as reported by the device, for example
 * `measured` or `stale`.
 * @property observedAt ISO-8601 UTC time the device took the measurement, if reported.
 * @property peopleNow number of people currently detected.
 * @property lookingNow number of detected people currently looking at the screen.
 * @property gender current counts keyed by gender name; empty when not provided.
 * @property age current counts keyed by age-group name; empty when not provided.
 * @property viewersTotal running total of viewers since the device's counters started.
 * @property avgDwellMs average dwell time in milliseconds.
 * @property reachTotal running total of unique people reached.
 * @property otsTotal running total of opportunities to see.
 * @property frequency average number of times each reached person was seen.
 * @property verifiedReachTotal running total of reach that the device could verify.
 * @property staffExcludedTotal running total of people excluded from the counts.
 * @property activeCampaignId id of the campaign whose play is currently on screen, or `null`
 * when none applies to this connection.
 */
data class AudienceSnapshot(
    val measurementStatus: String,
    val observedAt: String?,
    val peopleNow: Int?,
    val lookingNow: Int?,
    val gender: Map<String, Int>,
    val age: Map<String, Int>,
    val viewersTotal: Int?,
    val avgDwellMs: Long?,
    val reachTotal: Int?,
    val otsTotal: Int?,
    val frequency: Double?,
    val verifiedReachTotal: Int?,
    val staffExcludedTotal: Int?,
    val activeCampaignId: String?,
)
