package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

/**
 * One line of the on-screen activity log.
 *
 * @property timeMs when it was logged, in Unix time milliseconds.
 * @property isError `true` for failures, which are highlighted.
 * @property message text to show.
 */
data class LogEntry(val timeMs: Long, val isError: Boolean, val message: String)
