package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.BroadcastFeedState
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState

/**
 * Everything the screen needs to draw itself.
 *
 * @property settings the saved connection settings.
 * @property connection result of the last health check.
 * @property audience the latest live measurements, or `null` before any have arrived.
 * @property liveSource where [audience] came from, or `null` before any have arrived.
 * @property audienceError description of the last polling failure, or `null` while polling works.
 * @property polling whether live updates are switched on.
 * @property broadcast what the audience broadcast has delivered so far.
 * @property broadcastFresh `true` while the broadcast is arriving, so HTTP polling is paused.
 * @property recipient the values to register on the CMS connection so the broadcast reaches this app.
 * @property busy `true` while a play event is being delivered, so actions are disabled.
 * @property playback campaign and play state.
 * @property log activity log, newest entry first.
 */
data class AudienceUiState(
    val settings: ConnectionSettings,
    val connection: ConnectionStatus = ConnectionStatus.Unknown,
    val audience: AudienceSnapshot? = null,
    val liveSource: LiveSource? = null,
    val audienceError: String? = null,
    val polling: Boolean = false,
    val broadcast: BroadcastFeedState = BroadcastFeedState(),
    val broadcastFresh: Boolean = false,
    val recipient: BroadcastRecipient? = null,
    val busy: Boolean = false,
    val playback: PlaybackState = PlaybackState(),
    val log: List<LogEntry> = emptyList(),
)

/** Where a set of live figures came from. */
enum class LiveSource {
    /** Pushed by the DisplayPulse app as a broadcast, about once a second. */
    BROADCAST,

    /** Read from `GET /audience/current`, the fallback while the broadcast is not arriving. */
    HTTP,
}

/**
 * This app's identity as a broadcast recipient.
 *
 * @property packageName the application id.
 * @property signingCertificateSha256 SHA-256 of the signing certificate, or `null` if it cannot be read.
 */
data class BroadcastRecipient(val packageName: String, val signingCertificateSha256: String?)
