package com.lamasatech.displaypulse.samples.cmsintegration.domain

import java.util.UUID
import javax.inject.Inject

/** [IdGenerator] that produces random version-4 UUIDs. */
class UuidIdGenerator @Inject constructor() : IdGenerator {

    override fun newId(): String = UUID.randomUUID().toString()
}
