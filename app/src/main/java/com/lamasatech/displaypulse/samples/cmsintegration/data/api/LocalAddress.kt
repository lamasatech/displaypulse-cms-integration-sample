package com.lamasatech.displaypulse.samples.cmsintegration.data.api

import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/** Decides whether an address belongs to this device or a local network, never the public internet. */
object LocalAddress {

    /**
     * @param address a resolved IP address.
     * @return `true` for loopback, IPv4 private (10/8, 172.16/12, 192.168/16), IPv4 shared address
     * space (100.64/10, used by carrier NAT and by overlay networks such as Tailscale and ZeroTier),
     * link-local (169.254/16, fe80::/10) and IPv6 unique local (fc00::/7) addresses.
     */
    fun isLocal(address: InetAddress): Boolean {
        if (address.isLoopbackAddress || address.isLinkLocalAddress || address.isSiteLocalAddress) return true
        val bytes = address.address
        return when (address) {
            is Inet4Address -> bytes[0].toInt() and 0xFF == 100 && bytes[1].toInt() and 0xC0 == 0x40
            is Inet6Address -> bytes[0].toInt() and 0xFE == 0xFC
            else -> false
        }
    }
}
