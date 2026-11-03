package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.AudienceCurrentDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.toDomain
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import javax.inject.Inject

/**
 * Reads a broadcast payload. The payload has the same shape as the `GET /audience/current` body,
 * so it goes through the same DTO and mapper and absent fields stay `null`.
 *
 * @property gson the shared JSON converter.
 */
class AudienceUpdateParser @Inject constructor(private val gson: Gson) {

    /**
     * @param payloadJson the payload as received.
     * @return the measurements, or `null` if the payload is missing or is not a JSON object.
     */
    fun parse(payloadJson: String?): AudienceSnapshot? {
        if (payloadJson.isNullOrBlank()) return null
        return try {
            gson.fromJson(payloadJson, AudienceCurrentDto::class.java)?.toDomain()
        } catch (_: JsonParseException) {
            null
        }
    }
}
