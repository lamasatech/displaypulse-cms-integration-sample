package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The device's answer to `GET /health`.
 *
 * @property detail the reply as text, for the activity log.
 * @property playEventVersions the play events API versions the device accepts. A device that does
 * not list them predates the list and accepts [PlayEventsApi.BASE_VERSION] only.
 */
data class DeviceHealth(val detail: String, val playEventVersions: Set<String>) {
    /** `true` when the device accepts an advertiser on `started`. */
    val acceptsAdvertiser: Boolean get() = PlayEventsApi.ADVERTISER_VERSION in playEventVersions
}
