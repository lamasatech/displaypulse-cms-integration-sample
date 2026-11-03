package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

/**
 * Wire names of the audience broadcast. The receiver's protecting permission is declared in
 * `AndroidManifest.xml`, because Android checks it there before the broadcast reaches the app.
 */
object AudienceUpdateContract {

    /** Action of the broadcast. */
    const val ACTION = "com.lamasatech.audience.AUDIENCE_UPDATE"

    /** String extra holding the same JSON body as `GET /audience/current`. */
    const val EXTRA_PAYLOAD_JSON = "payload_json"
}
