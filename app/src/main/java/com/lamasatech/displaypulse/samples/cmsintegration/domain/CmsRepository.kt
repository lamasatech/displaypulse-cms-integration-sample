package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt

/**
 * Operations the app needs from the connected device.
 *
 * Every function reports problems through [ApiResult] and does not throw for network or HTTP failures.
 */
interface CmsRepository {

    /**
     * Checks that the device is reachable.
     *
     * @return a short text describing the device's reply.
     */
    suspend fun health(): ApiResult<String>

    /**
     * Reads the device's current measurements.
     *
     * @return the latest [AudienceSnapshot].
     */
    suspend fun audienceCurrent(): ApiResult<AudienceSnapshot>

    /**
     * Reports one play lifecycle event to the device.
     *
     * Sending the same [event] again is safe: the device recognises its event id and answers with
     * a duplicate acknowledgement instead of recording it twice.
     *
     * @param event the event to report.
     * @return the device's acknowledgement.
     */
    suspend fun sendPlayEvent(event: PlayEvent): ApiResult<PlayReceipt>
}
