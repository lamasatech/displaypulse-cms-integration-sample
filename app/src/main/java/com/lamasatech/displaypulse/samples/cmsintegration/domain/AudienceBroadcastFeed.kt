package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState
import kotlinx.coroutines.flow.StateFlow

/**
 * Live measurements pushed by the DisplayPulse app as a broadcast, about once a second, to the
 * apps registered on a CMS connection. Readers observe [state]; [AudienceBroadcastSink] writes it.
 */
interface AudienceBroadcastFeed {

    /** The latest broadcast and delivery counters; emits after every broadcast. */
    val state: StateFlow<BroadcastFeedState>
}

/** Entry point for broadcasts as they arrive. */
interface AudienceBroadcastSink {

    /**
     * Records one broadcast.
     *
     * @param payloadJson the broadcast's JSON payload, or `null` if it had none. An unreadable
     * payload is counted as rejected and leaves the latest measurements unchanged.
     */
    fun accept(payloadJson: String?)
}
