package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Running totals inside the current-measurements response.
 *
 * @property viewersTotal total viewers counted so far.
 * @property avgDwellMs average dwell time in milliseconds.
 * @property reachTotal total unique people reached.
 * @property otsTotal total opportunities to see.
 * @property frequency average times each reached person was seen.
 * @property verifiedReachTotal total reach the device could verify.
 * @property staffExcludedTotal total people excluded from the counts.
 */
data class SessionDto(
    @SerializedName("viewers_total") val viewersTotal: Int?,
    @SerializedName("avg_dwell_ms") val avgDwellMs: Long?,
    @SerializedName("reach_total") val reachTotal: Int?,
    @SerializedName("ots_total") val otsTotal: Int?,
    @SerializedName("frequency") val frequency: Double?,
    @SerializedName("verified_reach_total") val verifiedReachTotal: Int?,
    @SerializedName("staff_excluded_total") val staffExcludedTotal: Int?,
)
