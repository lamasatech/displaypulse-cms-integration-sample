package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

/** Whether the device answered the last health check. */
sealed interface ConnectionStatus {

    /** No check has been made yet. */
    data object Unknown : ConnectionStatus

    /**
     * The device answered.
     *
     * @property detail text of the device's reply.
     */
    data class Online(val detail: String) : ConnectionStatus

    /**
     * The device did not answer.
     *
     * @property reason short description of the failure.
     */
    data class Offline(val reason: String) : ConnectionStatus
}
