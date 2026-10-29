package com.lamasatech.displaypulse.samples.cmsintegration.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formats timestamps the way the receiver expects them. */
object Iso8601 {

    /**
     * Formats an instant as an ISO-8601 UTC string with millisecond precision, for example
     * `2023-11-14T22:13:20.000Z`.
     *
     * A new formatter is built per call because [SimpleDateFormat] is not thread-safe, and
     * `java.time` is avoided because it needs API 26 while this app supports API 24.
     *
     * @param unixMs the instant in Unix time milliseconds.
     * @return the formatted UTC timestamp.
     */
    fun formatUtc(unixMs: Long): String =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ROOT)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date(unixMs))
}
