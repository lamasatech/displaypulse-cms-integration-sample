package com.lamasatech.displaypulse.samples.cmsintegration.data.store

/**
 * How to reach the device.
 *
 * @property host IP address or host name of the device.
 * @property port TCP port the device listens on.
 * @property token access token sent with every protected request; blank means none is sent.
 */
data class ConnectionSettings(
    val host: String = "",
    val port: Int = DEFAULT_PORT,
    val token: String = "",
) {
    /** `true` when [host] is not blank and [port] is a valid TCP port, so requests can be attempted. */
    val isConfigured: Boolean get() = host.isNotBlank() && port in VALID_PORTS

    companion object {
        /** Port pre-filled for a new installation. */
        const val DEFAULT_PORT = 8400

        /** All valid TCP port numbers. */
        val VALID_PORTS = 1..65535

        /**
         * @param token an access token as entered, already trimmed.
         * @return `true` when the token can be sent in an HTTP header: blank (no token) or printable
         * ASCII without spaces. Characters picked up by copying from a document, such as a
         * non-breaking space or a typographic dash, make it `false`.
         */
        fun isValidToken(token: String): Boolean = token.all { it in '!'..'~' }
    }
}
