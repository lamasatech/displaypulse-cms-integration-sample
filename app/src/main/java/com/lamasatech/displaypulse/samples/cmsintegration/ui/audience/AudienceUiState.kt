package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState

/**
 * Everything the screen needs to draw itself.
 *
 * @property settings the saved connection settings.
 * @property connection result of the last health check.
 * @property audience the latest measurements, or `null` before the first successful poll.
 * @property audienceError description of the last polling failure, or `null` while polling works.
 * @property polling whether live polling is switched on.
 * @property busy `true` while a play event is being delivered, so actions are disabled.
 * @property playback campaign and play state.
 * @property log activity log, newest entry first.
 */
data class AudienceUiState(
    val settings: ConnectionSettings,
    val connection: ConnectionStatus = ConnectionStatus.Unknown,
    val audience: AudienceSnapshot? = null,
    val audienceError: String? = null,
    val polling: Boolean = false,
    val busy: Boolean = false,
    val playback: PlaybackState = PlaybackState(),
    val log: List<LogEntry> = emptyList(),
)
