package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import java.util.Locale

/**
 * How to reach the device.
 *
 * @property host host name of the device; one of [ALLOWED_HOSTS].
 * @property port TCP port the device listens on.
 * @property token site token sent with every protected request; blank means none is sent.
 */
data class ConnectionSettings(
    val host: String = DEFAULT_HOST,
    val port: Int = DEFAULT_PORT,
    val token: String = "",
) {
    companion object {
        /** The player runs on the same device as the DisplayPulse app. */
        const val DEFAULT_HOST = "127.0.0.1"

        /** Port pre-filled for a new installation. */
        const val DEFAULT_PORT = 8400

        /**
         * The only hosts the sample connects to. The network security config allows cleartext to
         * exactly these, so any other value could never reach the DisplayPulse app.
         */
        val ALLOWED_HOSTS = setOf(DEFAULT_HOST, "localhost")

        /** Shown when a host outside [ALLOWED_HOSTS] is entered or found in the stored settings. */
        const val HOST_REQUIREMENT =
            "Host must be 127.0.0.1 or localhost: the sample connects only to the DisplayPulse app on this device"

        /** All valid TCP port numbers. */
        val VALID_PORTS = 1..65535

        /**
         * @param host a host as typed by the user.
         * @return [host] without surrounding spaces and in lower case, the form compared with
         * [ALLOWED_HOSTS] and stored.
         */
        fun normalizeHost(host: String): String = host.trim().lowercase(Locale.ROOT)

        /**
         * @param host a host already passed through [normalizeHost].
         * @return `true` when [host] is one of [ALLOWED_HOSTS].
         */
        fun isAllowedHost(host: String): Boolean = host in ALLOWED_HOSTS

        /**
         * @param token a site token as entered, already trimmed.
         * @return `true` when the token can be sent in an HTTP header: blank (no token) or printable
         * ASCII without spaces. Characters picked up by copying from a document, such as a
         * non-breaking space or a typographic dash, make it `false`.
         */
        fun isValidToken(token: String): Boolean = token.all { it in '!'..'~' }
    }
}
