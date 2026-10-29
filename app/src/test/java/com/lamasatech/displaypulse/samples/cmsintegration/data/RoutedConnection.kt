package com.lamasatech.displaypulse.samples.cmsintegration.data

import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.ProxySelector
import java.net.Socket
import javax.net.SocketFactory
import okhttp3.Address
import okhttp3.Authenticator
import okhttp3.Connection
import okhttp3.ConnectionSpec
import okhttp3.Dns
import okhttp3.Handshake
import okhttp3.Protocol
import okhttp3.Route

/**
 * Test [Connection] that reports a route to [ip], so an interceptor can be checked against any
 * address without opening a socket.
 *
 * @param ip the address the connection claims to go to.
 */
class RoutedConnection(ip: String) : Connection {
    private val route = Route(
        Address(
            uriHost = ip, uriPort = 8400, dns = Dns.SYSTEM, socketFactory = SocketFactory.getDefault(),
            sslSocketFactory = null, hostnameVerifier = null, certificatePinner = null,
            proxyAuthenticator = Authenticator.NONE, proxy = Proxy.NO_PROXY,
            protocols = listOf(Protocol.HTTP_1_1), connectionSpecs = listOf(ConnectionSpec.CLEARTEXT),
            proxySelector = ProxySelector.getDefault(),
        ),
        Proxy.NO_PROXY,
        InetSocketAddress(InetAddress.getByName(ip), 8400),
    )

    override fun route(): Route = route

    override fun socket(): Socket = throw UnsupportedOperationException("not connected")

    override fun handshake(): Handshake? = null

    override fun protocol(): Protocol = Protocol.HTTP_1_1
}
