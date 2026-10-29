package com.lamasatech.displaypulse.samples.cmsintegration.data.store.dto

import com.google.gson.annotations.SerializedName

/**
 * On-disk form of the playback state. Every field is nullable because Gson does not enforce
 * Kotlin nullability; the mapper checks them when converting back.
 *
 * @property version format version of this record; a record with any other version is not read.
 * @property campaign the campaign new plays are started for.
 * @property activePlay the play the device has confirmed as started.
 * @property pending the event not yet acknowledged by the device.
 */
data class StoredPlaybackDto(
    @SerializedName("version") val version: Int?,
    @SerializedName("campaign") val campaign: StoredCampaignDto?,
    @SerializedName("active_play") val activePlay: StoredActivePlayDto?,
    @SerializedName("pending") val pending: StoredPlayEventDto?,
)
