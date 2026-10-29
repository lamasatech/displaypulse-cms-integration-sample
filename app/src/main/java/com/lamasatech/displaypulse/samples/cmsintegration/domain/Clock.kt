package com.lamasatech.displaypulse.samples.cmsintegration.domain

/** Source of the current wall-clock time, abstracted so time-dependent code can be tested. */
interface Clock {

    /** @return the current time in Unix time milliseconds. */
    fun nowMs(): Long
}
