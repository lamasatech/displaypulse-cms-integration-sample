package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeviceHealth

/** Whether the device answered the last health check. */
sealed interface ConnectionStatus {

    /** No check has been made yet. */
    data object Unknown : ConnectionStatus

    /**
     * The device answered.
     *
     * @property health the device's reply, including the play events API versions it accepts.
     */
    data class Online(val health: DeviceHealth) : ConnectionStatus

    /**
     * The device did not answer.
     *
     * @property reason short description of the failure.
     */
    data class Offline(val reason: String) : ConnectionStatus
}
