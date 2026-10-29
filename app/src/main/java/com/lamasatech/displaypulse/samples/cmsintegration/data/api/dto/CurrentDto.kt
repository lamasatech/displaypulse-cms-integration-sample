package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Live counts inside the current-measurements response.
 *
 * @property peopleNow number of people currently detected.
 * @property lookingNow number of detected people currently looking at the screen.
 * @property demographicsNow breakdown of the detected people, if provided.
 */
data class CurrentDto(
    @SerializedName("people_now") val peopleNow: Int?,
    @SerializedName("looking_now") val lookingNow: Int?,
    @SerializedName("demographics_now") val demographicsNow: DemographicsDto?,
)
