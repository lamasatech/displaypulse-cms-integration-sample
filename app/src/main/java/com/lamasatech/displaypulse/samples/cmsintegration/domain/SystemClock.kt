package com.lamasatech.displaypulse.samples.cmsintegration.domain

import javax.inject.Inject

/** [Clock] backed by the system wall clock and the JVM's monotonic clock. */
class SystemClock @Inject constructor() : Clock {

    override fun nowMs(): Long = System.currentTimeMillis()

    override fun elapsedMs(): Long = System.nanoTime() / NANOS_PER_MILLI

    private companion object {
        const val NANOS_PER_MILLI = 1_000_000L
    }
}
