package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.lamasatech.displaypulse.samples.cmsintegration.data.api.LocalAddress
import java.net.InetAddress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests which addresses count as local, including the edges of each range. */
class LocalAddressTest {

    private fun local(ip: String) = LocalAddress.isLocal(InetAddress.getByName(ip))

    @Test
    fun `loopback private and link-local IPv4 addresses are local`() {
        listOf(
            "127.0.0.1", "10.0.0.5", "10.255.255.255", "172.16.0.1", "172.31.255.255",
            "192.168.1.20", "169.254.10.1", "100.64.0.1", "100.100.1.1", "100.127.255.255",
        ).forEach { assertTrue(it, local(it)) }
    }

    @Test
    fun `public IPv4 addresses next to the private ranges are not local`() {
        listOf(
            "8.8.8.8", "9.255.255.255", "11.0.0.1", "172.15.255.255", "172.32.0.1",
            "192.167.1.1", "192.169.0.1", "100.63.255.255", "100.128.0.1", "169.253.0.1",
        ).forEach { assertFalse(it, local(it)) }
    }

    @Test
    fun `loopback link-local and unique local IPv6 addresses are local`() {
        listOf("::1", "fe80::1", "fc00::1", "fd12:3456::1").forEach { assertTrue(it, local(it)) }
    }

    @Test
    fun `public IPv6 addresses are not local`() {
        listOf("2001:4860:4860::8888", "fe00::1", "fb00::1").forEach { assertFalse(it, local(it)) }
    }
}
