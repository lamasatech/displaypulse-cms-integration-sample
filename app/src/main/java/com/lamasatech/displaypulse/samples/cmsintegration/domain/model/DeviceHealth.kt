package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The device's answer to `GET /health`.
 *
 * @property detail the reply as text, for the activity log.
 * @property playEventVersions the play events API versions the device lists. Empty when it lists
 * none, which only a DisplayPulse app older than play events API [PlayEventsApi.VERSION] does.
 */
data class DeviceHealth(val detail: String, val playEventVersions: Set<String>) {
    /** `true` when the device accepts the play events this app sends. */
    val acceptsPlayEvents: Boolean get() = PlayEventsApi.VERSION in playEventVersions
}
