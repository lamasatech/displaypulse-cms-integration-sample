package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState

/**
 * Decides whether the audience broadcast is still arriving. The DisplayPulse app sends it about
 * once a second, so a few seconds without a readable one means it has stopped: the app is not
 * registered, the feature restarted, or the payloads cannot be read.
 */
object BroadcastFreshness {

    /** Longest gap since the last readable broadcast before it counts as stopped. */
    const val MAX_AGE_MS = 3_000L

    /**
     * @param feed what the broadcast has delivered so far.
     * @param nowElapsedMs the current [Clock.elapsedMs].
     * @return `true` if a readable broadcast arrived within [MAX_AGE_MS]; `false` before the first one.
     */
    fun isFresh(feed: BroadcastFeedState, nowElapsedMs: Long): Boolean {
        val receivedAt = feed.receivedAtElapsedMs ?: return false
        return nowElapsedMs - receivedAt in 0..MAX_AGE_MS
    }
}
