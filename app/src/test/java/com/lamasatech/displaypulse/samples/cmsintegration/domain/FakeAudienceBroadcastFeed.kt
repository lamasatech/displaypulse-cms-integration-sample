package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Test [AudienceBroadcastFeed] whose state a test sets directly.
 *
 * @property state the feed state; assign `state.value` to simulate a broadcast.
 */
class FakeAudienceBroadcastFeed : AudienceBroadcastFeed {
    override val state = MutableStateFlow(BroadcastFeedState())
}
