package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The expected content duration a `started` event may declare as `expected_duration_ms`.
 *
 * The DisplayPulse app keeps a play open for this duration plus a grace period, then ends it on its
 * own if no closing event arrived. Without a duration it ends the play after its fallback limit
 * (5 minutes), so content longer than that should always declare one. The device refuses a value
 * outside [MIN_MS]..[MAX_MS] instead of clamping it.
 */
object ContentDuration {
    const val MIN_MS = 5_000L
    const val MAX_MS = 7_200_000L

    private const val MS_PER_SECOND = 1_000L

    /**
     * @param durationMs a duration in milliseconds.
     * @return `true` when the device accepts it.
     */
    fun isValid(durationMs: Long): Boolean = durationMs in MIN_MS..MAX_MS

    /**
     * @param text a whole number of seconds, as typed by the user.
     * @return the duration in milliseconds, or `null` when [text] is not a whole number of seconds
     * the device accepts.
     */
    fun fromSecondsText(text: String): Long? = text.trim().toLongOrNull()
        ?.takeIf { it in MIN_MS / MS_PER_SECOND..MAX_MS / MS_PER_SECOND }
        ?.times(MS_PER_SECOND)
}
