package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * The device's acknowledgement of one play event.
 *
 * @property receiptId id the device assigned to the play record, if it returned one.
 * @property duplicate `true` when the device had already stored an identical event, which means
 * this call was a retry rather than a new event.
 */
data class PlayReceipt(val receiptId: Long?, val duplicate: Boolean)
