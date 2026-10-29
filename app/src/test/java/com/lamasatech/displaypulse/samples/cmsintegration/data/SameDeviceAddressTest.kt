package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.api.SameDeviceAddress
import java.net.InetAddress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests that only this device's own addresses are accepted. */
class SameDeviceAddressTest {

    private fun sameDevice(ip: String) = SameDeviceAddress.isSameDevice(InetAddress.getByName(ip))

    @Test
    fun `loopback addresses are this device`() {
        listOf("127.0.0.1", "127.0.0.53", "127.255.255.254", "::1").forEach { assertTrue(it, sameDevice(it)) }
    }

    @Test
    fun `local network addresses are not this device`() {
        listOf(
            "10.0.0.5", "172.16.0.1", "192.168.1.20", "169.254.10.1", "100.64.0.1",
            "fe80::1", "fd12:3456::1",
        ).forEach { assertFalse(it, sameDevice(it)) }
    }

    @Test
    fun `public addresses are not this device`() {
        listOf("8.8.8.8", "128.0.0.1", "126.255.255.255", "2001:4860:4860::8888").forEach { assertFalse(it, sameDevice(it)) }
    }
}
