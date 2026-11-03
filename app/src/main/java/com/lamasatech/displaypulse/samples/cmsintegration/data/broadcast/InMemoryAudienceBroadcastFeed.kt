package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import com.lamasatech.displaypulse.samples.cmsintegration.domain.AudienceBroadcastFeed
import com.lamasatech.displaypulse.samples.cmsintegration.domain.AudienceBroadcastSink
import com.lamasatech.displaypulse.samples.cmsintegration.domain.Clock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Keeps the latest broadcast for the life of the process. One instance serves as both the
 * receiver's sink and the screen's feed, so broadcasts received while the screen is closed are
 * there when it opens.
 *
 * @property parser reads each payload.
 * @property clock timestamps each readable broadcast on its monotonic clock.
 */
@Singleton
class InMemoryAudienceBroadcastFeed @Inject constructor(
    private val parser: AudienceUpdateParser,
    private val clock: Clock,
) : AudienceBroadcastFeed, AudienceBroadcastSink {

    private val _state = MutableStateFlow(BroadcastFeedState())
    override val state: StateFlow<BroadcastFeedState> = _state.asStateFlow()

    override fun accept(payloadJson: String?) {
        val snapshot = parser.parse(payloadJson)
        val receivedAtElapsedMs = clock.elapsedMs()
        _state.update { current ->
            if (snapshot == null) {
                current.copy(rejected = current.rejected + 1)
            } else {
                current.copy(latest = snapshot, receivedAtElapsedMs = receivedAtElapsedMs, received = current.received + 1)
            }
        }
    }
}
