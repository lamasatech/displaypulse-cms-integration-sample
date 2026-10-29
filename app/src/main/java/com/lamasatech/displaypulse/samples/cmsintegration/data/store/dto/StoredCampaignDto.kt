package com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto

import com.google.gson.annotations.SerializedName

/**
 * On-disk form of a campaign.
 *
 * @property id campaign id.
 * @property name display name.
 * @property contentId id of the content item played for the campaign.
 */
data class StoredCampaignDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("content_id") val contentId: String?,
)
