package com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto

import com.google.gson.annotations.SerializedName

/**
 * Breakdown of the currently detected people.
 *
 * @property gender counts keyed by gender name.
 * @property age counts keyed by age-group name.
 */
data class DemographicsDto(
    @SerializedName("gender") val gender: Map<String, Int>?,
    @SerializedName("age") val age: Map<String, Int>?,
)
