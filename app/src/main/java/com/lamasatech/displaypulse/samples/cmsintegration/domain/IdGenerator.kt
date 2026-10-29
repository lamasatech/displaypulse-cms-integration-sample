package com.lamasatech.displaypulse.samples.cmsintegration.domain

/** Source of unique identifiers, abstracted so id-dependent code can be tested deterministically. */
interface IdGenerator {

    /** @return a new identifier that has not been returned before. */
    fun newId(): String
}
