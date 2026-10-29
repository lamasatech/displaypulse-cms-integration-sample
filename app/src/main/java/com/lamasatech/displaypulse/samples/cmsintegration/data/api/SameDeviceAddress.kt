package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import java.net.InetAddress

/**
 * Decides whether an address is this device. The sample supports a player on the same device as
 * the DisplayPulse app only; players on another device on the local network are not supported yet.
 */
object SameDeviceAddress {

    /**
     * @param address a resolved IP address.
     * @return `true` for loopback addresses (127.0.0.0/8 and ::1), `false` for every other address,
     * including private local-network ranges.
     */
    fun isSameDevice(address: InetAddress): Boolean = address.isLoopbackAddress
}
