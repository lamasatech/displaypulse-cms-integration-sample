package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Response body for an accepted play event.
 *
 * @property status outcome name, for example `accepted` or `duplicate`.
 * @property playId id of the play the event belonged to.
 * @property receiptId id the device assigned to the play record.
 */
data class PlayEventResponseDto(
    @SerializedName("status") val status: String?,
    @SerializedName("play_id") val playId: String?,
    @SerializedName("receipt_id") val receiptId: Long?,
)
