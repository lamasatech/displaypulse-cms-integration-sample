package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The lifecycle stages of a single play.
 *
 * @property wireName the value sent in the request's `event_type` field.
 */
enum class PlayEventType(val wireName: String) {
    /** The play began on screen. */
    STARTED("started"),

    /** The play ran to its end. */
    COMPLETED("completed"),

    /** The play could not continue because of an error. */
    FAILED("failed"),

    /** The play was cut short before it ended. */
    INTERRUPTED("interrupted");

    /** `true` for every type that closes a play. */
    val isTerminal: Boolean get() = this != STARTED

    /** `true` for types that must explain themselves with a reason. */
    val requiresReason: Boolean get() = this == FAILED || this == INTERRUPTED
}
