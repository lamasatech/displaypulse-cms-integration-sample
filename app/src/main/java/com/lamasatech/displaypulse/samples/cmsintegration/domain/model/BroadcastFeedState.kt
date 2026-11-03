package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * What the app has received so far from the audience broadcast.
 *
 * @property latest the measurements from the most recent readable broadcast, or `null` before one
 * has arrived.
 * @property receivedAtElapsedMs [com.lamasatech.displaypulse.samples.cmsintegration.domain.Clock.elapsedMs]
 * at which [latest] was received, used to tell whether the broadcast is still arriving.
 * @property received number of readable broadcasts received since the app started.
 * @property rejected number of broadcasts whose payload could not be read. They never replace
 * [latest].
 */
data class BroadcastFeedState(
    val latest: AudienceSnapshot? = null,
    val receivedAtElapsedMs: Long? = null,
    val received: Int = 0,
    val rejected: Int = 0,
)
