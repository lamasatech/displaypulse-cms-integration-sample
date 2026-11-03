package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/** The version of `POST /v1/play-events` this app sends. */
object PlayEventsApi {
    /**
     * Sent as `schema_version` on every event. A device accepts it only when `GET /health` lists
     * it in `play_events_schema_versions`.
     */
    const val VERSION = "1.2"
}
