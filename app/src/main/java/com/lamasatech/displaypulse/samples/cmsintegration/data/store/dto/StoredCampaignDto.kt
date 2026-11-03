package com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto

import com.google.gson.annotations.SerializedName

/**
 * On-disk form of a campaign.
 *
 * @property id campaign id.
 * @property name display name.
 * @property contentId id of the content item played for the campaign.
 * @property advertiser the brand behind the campaign. Records written before 1.3.0 do not have it
 * and read back as none.
 */
data class StoredCampaignDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("content_id") val contentId: String?,
    @SerializedName("advertiser") val advertiser: String? = null,
)
