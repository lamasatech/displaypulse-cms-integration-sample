package com.lamasatech.displaypulse.samples.cmsintegration.domain

/**
 * Test [Clock] that stays at one instant until a test moves it.
 *
 * @property nowMs the time returned by [nowMs]; assign to move the clock.
 */
class FixedClock(var nowMs: Long = 1_700_000_000_000L) : Clock {
    override fun nowMs() = nowMs
}
