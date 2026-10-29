package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt

/**
 * Test [CmsRepository] that answers `sendPlayEvent` from a queue of prepared replies.
 *
 * @property replies replies returned in order, one per call.
 * @property sentEventIds ids of the events received, in call order.
 */
class ScriptedCmsRepository(private val replies: ArrayDeque<ApiResult<PlayReceipt>>) : CmsRepository {
    val sentEventIds = mutableListOf<String>()

    override suspend fun health(): ApiResult<String> = error("not scripted")

    override suspend fun audienceCurrent(): ApiResult<AudienceSnapshot> = error("not scripted")

    override suspend fun sendPlayEvent(event: PlayEvent): ApiResult<PlayReceipt> {
        sentEventIds += event.eventId
        return replies.removeFirst()
    }
}
