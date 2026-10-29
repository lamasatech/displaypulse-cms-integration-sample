package com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto

import com.google.gson.annotations.SerializedName

/**
 * On-disk form of the open play.
 *
 * @property playId id of the play.
 * @property campaignId campaign the play belongs to.
 * @property contentId content item being played.
 * @property startedAtMs start time in Unix time milliseconds.
 */
data class StoredActivePlayDto(
    @SerializedName("play_id") val playId: String?,
    @SerializedName("campaign_id") val campaignId: String?,
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("started_at_ms") val startedAtMs: Long?,
)
