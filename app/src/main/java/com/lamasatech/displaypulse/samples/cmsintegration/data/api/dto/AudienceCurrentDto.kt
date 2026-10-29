package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Wire model of the device's current-measurements response.
 *
 * Gson bypasses Kotlin constructors, so every field is nullable: any field may be absent from a response.
 *
 * @property measurementStatus freshness of the data, for example `measured` or `stale`.
 * @property observedAt ISO-8601 UTC time of the measurement.
 * @property current live counts, or `null` when they cannot be measured right now.
 * @property session running totals, present only when the connection may see them.
 * @property activeCampaignId campaign of the play currently on screen, if it applies to this connection.
 */
data class AudienceCurrentDto(
    @SerializedName("measurement_status") val measurementStatus: String?,
    @SerializedName("observed_at") val observedAt: String?,
    @SerializedName("current") val current: CurrentDto?,
    @SerializedName("session") val session: SessionDto?,
    @SerializedName("active_campaign_id") val activeCampaignId: String?,
)
