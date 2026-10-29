package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import kotlinx.coroutines.delay

/**
 * Test [CmsRepository] for view-model tests: every call answers from prepared replies.
 *
 * @property healthReplies health replies in call order, each after its own delay in milliseconds.
 * @property audienceReply the reply to every live-data request; assign to change it.
 * @property playReplies play-event replies in call order.
 * @property audienceCalls number of live-data requests made.
 * @property sentEvents the play events received, in call order.
 * @property onPlayEvent called with each play event as it arrives, before the reply.
 */
class FakeCmsRepository : CmsRepository {
    val healthReplies = ArrayDeque<Pair<Long, ApiResult<String>>>()
    var audienceReply: ApiResult<AudienceSnapshot> = ApiResult.NetworkError("not scripted")
    val playReplies = ArrayDeque<ApiResult<PlayReceipt>>()
    var audienceCalls = 0
    val sentEvents = mutableListOf<PlayEvent>()
    var onPlayEvent: (PlayEvent) -> Unit = {}

    override suspend fun health(): ApiResult<String> {
        val (delayMs, reply) = healthReplies.removeFirst()
        delay(delayMs)
        return reply
    }

    override suspend fun audienceCurrent(): ApiResult<AudienceSnapshot> {
        audienceCalls++
        return audienceReply
    }

    override suspend fun sendPlayEvent(event: PlayEvent): ApiResult<PlayReceipt> {
        sentEvents += event
        onPlayEvent(event)
        return playReplies.removeFirst()
    }
}
