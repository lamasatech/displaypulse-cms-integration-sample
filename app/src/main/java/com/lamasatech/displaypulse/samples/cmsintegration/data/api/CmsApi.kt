package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import com.google.gson.JsonObject
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.AudienceCurrentDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.PlayEventRequestDto
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.dto.PlayEventResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * HTTP API of the connected device.
 *
 * Host, port and access token are not part of these declarations: [CmsEndpointInterceptor]
 * applies them to every request. Functions return [Response] so callers can read the status
 * code and headers as well as the body.
 */
interface CmsApi {

    /**
     * Checks that the device is reachable. Needs no access token.
     *
     * @return the device's reply as a JSON object.
     */
    @GET("health")
    suspend fun health(): Response<JsonObject>

    /**
     * Reads the device's current measurements.
     *
     * @return the measurements; fields the connection may not see are absent.
     */
    @GET("audience/current")
    suspend fun audienceCurrent(): Response<AudienceCurrentDto>

    /**
     * Reports one play lifecycle event.
     *
     * @param body the event to report.
     * @return the acknowledgement; status 202 for a new event and 200 for an identical retry.
     */
    @POST("v1/play-events")
    suspend fun postPlayEvent(@Body body: PlayEventRequestDto): Response<PlayEventResponseDto>
}
