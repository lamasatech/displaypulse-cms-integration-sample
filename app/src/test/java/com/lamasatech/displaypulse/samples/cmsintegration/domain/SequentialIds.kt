package com.lamasatech.displaypulse.samples.cmsintegration.domain

/** Test [IdGenerator] returning "id-1", "id-2", ... so tests can assert exactly which id was reused. */
class SequentialIds : IdGenerator {
    private var next = 1
    override fun newId() = "id-${next++}"
}
