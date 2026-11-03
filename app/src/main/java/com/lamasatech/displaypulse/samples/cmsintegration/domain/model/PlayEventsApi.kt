package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * Versions of `POST /v1/play-events` this app sends.
 *
 * Every device accepts [BASE_VERSION]. [ADVERTISER_VERSION] adds `advertiser` on `started`, and a
 * device accepts it only when `GET /health` lists it, so an event uses it only when it names an
 * advertiser. Each event keeps the version it was created with, which makes a retry identical.
 */
object PlayEventsApi {
    const val BASE_VERSION = "1.1"
    const val ADVERTISER_VERSION = "1.2"

    /**
     * @param event the event to send.
     * @return the `schema_version` for [event].
     */
    fun versionFor(event: PlayEvent): String =
        if (event.advertiser != null) ADVERTISER_VERSION else BASE_VERSION
}
