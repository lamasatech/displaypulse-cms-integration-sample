package com.lamasatech.displaypulse.samples.cmsintegration.domain

/**
 * Test [Clock] that stays at one instant until a test moves it.
 *
 * @property nowMs the time returned by [nowMs]; assign to move the clock.
 * @property elapsedMs the time returned by [elapsedMs]; assign to move the monotonic clock.
 */
class FixedClock(var nowMs: Long = 1_700_000_000_000L, var elapsedMs: Long = 50_000L) : Clock {
    override fun nowMs() = nowMs
    override fun elapsedMs() = elapsedMs
}
