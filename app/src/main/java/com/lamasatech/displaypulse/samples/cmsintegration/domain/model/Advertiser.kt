package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The advertiser a `started` event may name (play events API [PlayEventsApi.ADVERTISER_VERSION]).
 *
 * It names the brand behind a campaign, so DisplayPulse reports show it instead of `_unknown`.
 * These are the device's rules: it refuses a value that breaks them instead of shortening or
 * dropping it, so the app checks them before anything is sent.
 */
object Advertiser {
    /** Longest advertiser, in UTF-16 code units after trimming. */
    const val MAX_LENGTH = 120

    /** DisplayPulse's placeholder for "no advertiser"; it cannot be sent as a real name. */
    const val RESERVED = "_unknown"

    /**
     * @param text the advertiser as typed by the user.
     * @return [text] without surrounding whitespace, or `null` when the device would refuse it:
     * blank, longer than [MAX_LENGTH], containing a control character, or [RESERVED].
     */
    fun normalize(text: String): String? = text.trim().takeIf {
        it.isNotEmpty() && it.length <= MAX_LENGTH && it.none(Char::isISOControl) && it != RESERVED
    }

    /** @return `true` when [value] is already in the form [normalize] returns. */
    fun isNormalized(value: String): Boolean = normalize(value) == value
}
