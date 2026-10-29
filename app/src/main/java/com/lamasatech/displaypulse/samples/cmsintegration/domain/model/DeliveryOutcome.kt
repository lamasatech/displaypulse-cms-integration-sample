package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/** What the caller should do with a pending play event after a delivery attempt. */
enum class DeliveryOutcome {

    /** The device accepted the event, or recognised it as an identical retry. */
    ACKNOWLEDGED,

    /** The device refused the event for good; sending the same event again cannot succeed. */
    REJECTED,

    /** The device refused a terminal event because it already ended this play (for example its open-play timeout fired): the play is over, so close it locally. */
    ALREADY_ENDED,

    /** The failure is transient or fixable (offline, wrong token, device busy): keep the event and resend it unchanged. */
    RETRYABLE,
}
