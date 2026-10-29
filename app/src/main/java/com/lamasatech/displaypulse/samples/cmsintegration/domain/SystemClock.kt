package com.lamasatech.displaypulse.samples.cmsintegration.domain

import javax.inject.Inject

/** [Clock] backed by the system wall clock. */
class SystemClock @Inject constructor() : Clock {

    override fun nowMs(): Long = System.currentTimeMillis()
}
