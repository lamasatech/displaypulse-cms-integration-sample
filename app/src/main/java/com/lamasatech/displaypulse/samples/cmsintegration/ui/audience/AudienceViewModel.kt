package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PlaybackStore
import com.lamasatech.displaypulse.samples.cmsintegration.domain.CampaignFactory
import com.lamasatech.displaypulse.samples.cmsintegration.domain.Clock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.CmsRepository
import com.lamasatech.displaypulse.samples.cmsintegration.domain.DeliveryOutcomeClassifier
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlayEventSender
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlaybackReducer
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ContentDuration
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.DeliveryOutcome
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Holds the state of the main screen and runs its actions.
 *
 * It owns three jobs: saving the connection and checking it, polling live measurements while the
 * screen is visible, and driving the campaign / play flow with safe retries.
 *
 * The playback state is saved to disk before each event is sent and after each reply. When the
 * app starts with an event still pending from an earlier run, it resends that event with the same
 * ids, so a play is reported once even if the app or the device restarts mid-play.
 *
 * @property settingsStore saved connection settings.
 * @property playbackStore saved campaign, open play and pending event.
 * @property campaignFactory creates new campaigns.
 * @property repository the device connection.
 * @property sender delivers play events with busy-reply handling.
 * @property outcomes decides what to do with a delivery result.
 * @property reducer playback state transitions.
 * @property clock timestamps for log entries.
 */
@HiltViewModel
class AudienceViewModel @Inject constructor(
    private val settingsStore: ConnectionSettingsStore,
    private val playbackStore: PlaybackStore,
    private val campaignFactory: CampaignFactory,
    private val repository: CmsRepository,
    private val sender: PlayEventSender,
    private val outcomes: DeliveryOutcomeClassifier,
    private val reducer: PlaybackReducer,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AudienceUiState(settings = settingsStore.load())
    )
    /** The current screen state; emits a new value after every change. */
    val state: StateFlow<AudienceUiState> = _state.asStateFlow()

    private val deliveryLock = Mutex()
    private val saveLock = Mutex()
    private var healthJob: Job? = null
    private var pollJob: Job? = null
    private var screenVisible = false

    init {
        restorePlayback()
    }

    /**
     * Validates and saves the connection settings, then checks that the device answers.
     *
     * Nothing is saved, and the reason is logged, when the host, port or token is invalid, or when
     * the device address would change while a play is open or an event is waiting. The token alone
     * may change at any time, so a rejected token can be fixed before tapping Retry.
     *
     * @param host host as typed by the user; only `127.0.0.1` and `localhost` are accepted.
     * @param portText port as typed by the user.
     * @param token site token; blank means none.
     */
    fun saveConnection(host: String, portText: String, token: String) {
        val normalizedHost = ConnectionSettings.normalizeHost(host)
        if (!ConnectionSettings.isAllowedHost(normalizedHost)) {
            log(ConnectionSettings.HOST_REQUIREMENT, isError = true)
            return
        }
        val port = portText.trim().toIntOrNull()
        if (port == null || port !in ConnectionSettings.VALID_PORTS) {
            log("Port must be a number between 1 and 65535", isError = true)
            return
        }
        val settings = ConnectionSettings(normalizedHost, port, token.trim())
        if (!ConnectionSettings.isValidToken(settings.token)) {
            log("Site token contains a space or a character that cannot be sent; paste it again", isError = true)
            return
        }
        val current = _state.value
        val deviceChanged = settings.host != current.settings.host || settings.port != current.settings.port
        if (deviceChanged && !current.playback.canChangeDevice) {
            log("Finish or discard the current play before changing the device address", isError = true)
            return
        }
        val tokenStored = settingsStore.save(settings)
        _state.update { it.copy(settings = settings) }
        log("Saved connection ${settings.host}:${settings.port}")
        if (!tokenStored) {
            log("The site token could not be stored securely; it is used until the app closes", isError = true)
        }
        checkHealth()
    }

    /**
     * Asks the device whether it is reachable and updates [AudienceUiState.connection]. A check
     * still running is cancelled first, so a slow answer about an old address cannot overwrite a
     * newer one.
     */
    fun checkHealth() {
        healthJob?.cancel()
        healthJob = viewModelScope.launch {
            when (val result = repository.health()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(connection = ConnectionStatus.Online(result.value)) }
                    log("GET /health → 200 ${result.value}")
                }
                is ApiResult.HttpError -> markOffline("HTTP ${result.code}")
                is ApiResult.NetworkError -> markOffline(result.message)
            }
        }
    }

    /**
     * Creates and saves a new campaign with a fresh UUID. Refused, with a log message, while a play
     * is open or an event is waiting to be delivered.
     *
     * @param name display name; a blank name gets a default.
     */
    fun createCampaign(name: String) {
        if (!_state.value.playback.canChangeCampaign) {
            log("Finish the current play before creating a new campaign", isError = true)
            return
        }
        val campaign = campaignFactory.create(name)
        _state.update { it.copy(playback = reducer.withCampaign(it.playback, campaign)) }
        log("Campaign \"${campaign.name}\" created: ${campaign.id}")
        viewModelScope.launch { savePlayback() }
    }

    /**
     * Starts a new play for the current campaign and delivers its `started` event. Does nothing if
     * a play cannot start now.
     *
     * Nothing is sent, and the reason is logged, when [durationSecondsText] is not blank and is not
     * a whole number of seconds the device accepts.
     *
     * @param durationSecondsText expected content duration in whole seconds as typed by the user;
     * blank means unknown, and the device then ends the play after its fallback limit.
     */
    fun startPlay(durationSecondsText: String) {
        if (!_state.value.playback.canStart) return
        val expectedDurationMs = if (durationSecondsText.isBlank()) null else {
            ContentDuration.fromSecondsText(durationSecondsText) ?: run {
                log("Content duration must be a whole number of seconds from " +
                    "${ContentDuration.MIN_MS / 1_000} to ${ContentDuration.MAX_MS / 1_000}", isError = true)
                return
            }
        }
        _state.update { it.copy(playback = reducer.requestStart(it.playback, expectedDurationMs)) }
        deliverPending()
    }

    /**
     * Closes the open play and delivers the terminal event. Does nothing if no play can be closed now.
     *
     * @param type how the play ended; must be a terminal type.
     * @param reason explanation, required for failed and interrupted plays.
     */
    fun endPlay(type: PlayEventType, reason: String?) {
        if (!_state.value.playback.canEnd) return
        _state.update { it.copy(playback = reducer.requestEnd(it.playback, type, reason)) }
        deliverPending()
    }

    /** Resends the pending event unchanged: the same event id lets the device treat it as a retry. */
    fun retryPending() = deliverPending()

    /**
     * Gives up on the pending event, for when the device keeps refusing it and retrying cannot help.
     * Waits for a delivery in progress to finish first; does nothing if no event is left pending.
     */
    fun discardPending() {
        viewModelScope.launch {
            deliveryLock.withLock {
                val event = _state.value.playback.pending ?: return@withLock
                _state.update { it.copy(playback = reducer.discarded(it.playback)) }
                savePlayback()
                val effect = if (event.type.isTerminal) {
                    "play closed here; if the device still holds it, it ends it after its open-play timeout"
                } else {
                    "no play was opened; if the device did receive it, it ends that play after its open-play timeout"
                }
                log("Discarded ${event.type.wireName} event ${event.eventId}: $effect", isError = true)
            }
        }
    }

    /**
     * Switches live polling on or off. Polling only runs while the screen is also visible.
     *
     * @param enabled `true` to poll.
     */
    fun setPolling(enabled: Boolean) {
        _state.update { it.copy(polling = enabled) }
        refreshPolling()
    }

    /**
     * Tells the view model whether the screen is on display, so polling can pause in the background.
     *
     * @param visible `true` while the screen is started.
     */
    fun onScreenVisible(visible: Boolean) {
        screenVisible = visible
        refreshPolling()
    }

    private fun deliverPending() {
        viewModelScope.launch {
            deliveryLock.withLock {
                val event = _state.value.playback.pending ?: return@withLock
                _state.update { it.copy(busy = true) }
                // On disk before it goes out: if the app dies during the request, the next start resends it.
                savePlayback()
                val result = sender.send(event) { waitMs ->
                    log("Device rate limit hit; resending ${event.type.wireName} in ${waitMs} ms", isError = true)
                }
                applyDelivery(event, result)
                _state.update { it.copy(busy = false) }
            }
        }
    }

    private suspend fun applyDelivery(event: PlayEvent, result: ApiResult<PlayReceipt>) {
        val outcome = outcomes.classify(result)
        log(describe(event, result), isError = outcome != DeliveryOutcome.ACKNOWLEDGED)
        _state.update {
            val playback = when (outcome) {
                DeliveryOutcome.ACKNOWLEDGED -> reducer.acknowledged(it.playback)
                DeliveryOutcome.REJECTED -> reducer.rejected(it.playback)
                DeliveryOutcome.ALREADY_ENDED -> reducer.endedByDevice(it.playback)
                DeliveryOutcome.RETRYABLE -> it.playback
            }
            it.copy(playback = playback)
        }
        savePlayback()
    }

    private fun restorePlayback() {
        val saved = playbackStore.load()
        if (saved == null) {
            log("The saved play state could not be read, so the app starts without it", isError = true)
            return
        }
        _state.update { it.copy(playback = saved) }
        val pending = saved.pending ?: return
        log("Resending the ${pending.type.wireName} event ${pending.eventId} saved before the app closed")
        deliverPending()
    }

    /**
     * Writes the current playback state to disk. Each write takes the latest state under [saveLock],
     * so writes that finish out of order can never leave an older state on disk.
     */
    private suspend fun savePlayback() {
        val saved = saveLock.withLock { playbackStore.save(_state.value.playback) }
        if (!saved) log("Could not save the play state; if the app closes now, the pending event is lost", isError = true)
    }

    private fun describe(event: PlayEvent, result: ApiResult<PlayReceipt>): String {
        val duration = event.expectedDurationMs?.let { " expected_duration_ms=$it" }.orEmpty()
        val head = "POST /v1/play-events ${event.type.wireName} play=${event.playId.take(8)}$duration"
        return when (result) {
            is ApiResult.Success ->
                "$head → ${if (result.value.duplicate) "200 duplicate" else "202 accepted"} receipt=${result.value.receiptId}"
            is ApiResult.HttpError -> {
                val failure = "$head → HTTP ${result.code} ${result.error.orEmpty()} ${result.message.orEmpty()}".trim()
                when (outcomes.classify(result)) {
                    DeliveryOutcome.RETRYABLE -> "$failure (kept pending, tap Retry)"
                    DeliveryOutcome.ALREADY_ENDED -> "$failure (device already ended this play; closed it here)"
                    else -> failure
                }
            }
            is ApiResult.NetworkError -> "$head → ${result.message} (kept pending, tap Retry)"
        }
    }

    private fun refreshPolling() {
        pollJob?.cancel()
        pollJob = null
        if (!_state.value.polling || !screenVisible) return
        pollJob = viewModelScope.launch {
            while (isActive) {
                val nextPollMs = pollOnce() ?: break
                delay(nextPollMs)
            }
        }
    }

    /** @return how long to wait before the next poll, or `null` when polling must stop. */
    private suspend fun pollOnce(): Long? = when (val result = repository.audienceCurrent()) {
        is ApiResult.Success -> {
            _state.update { it.copy(audience = result.value, audienceError = null) }
            POLL_INTERVAL_MS
        }
        is ApiResult.HttpError -> when (result.code) {
            // Not an error: the device asked for a pause, so wait as long as it asked.
            HTTP_TOO_MANY_REQUESTS -> (result.retryAfterMs ?: POLL_INTERVAL_MS).coerceIn(POLL_INTERVAL_MS, MAX_POLL_WAIT_MS)
            // Repeating the same request cannot succeed until the user fixes the token or the plan.
            in STOP_POLLING_CODES -> {
                stopPolling("HTTP ${result.code} ${result.error.orEmpty()}".trim())
                null
            }
            else -> {
                reportPollError("HTTP ${result.code} ${result.error.orEmpty()}".trim())
                POLL_INTERVAL_MS
            }
        }
        is ApiResult.NetworkError -> {
            reportPollError(result.message)
            POLL_INTERVAL_MS
        }
    }

    private fun stopPolling(reason: String) {
        _state.update { it.copy(polling = false, audienceError = reason) }
        log("Live updates stopped: GET /audience/current → $reason. Fix it, then switch Live updates on again", isError = true)
    }

    private fun reportPollError(message: String) {
        val firstOccurrence = _state.value.audienceError != message
        _state.update { it.copy(audienceError = message) }
        if (firstOccurrence) log("GET /audience/current failed: $message", isError = true)
    }

    private fun markOffline(reason: String) {
        _state.update { it.copy(connection = ConnectionStatus.Offline(reason)) }
        log("GET /health failed: $reason", isError = true)
    }

    private fun log(message: String, isError: Boolean = false) {
        val entry = LogEntry(clock.nowMs(), isError, message)
        _state.update { it.copy(log = (listOf(entry) + it.log).take(MAX_LOG_ENTRIES)) }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 1_200L
        const val MAX_POLL_WAIT_MS = 30_000L
        const val HTTP_TOO_MANY_REQUESTS = 429
        val STOP_POLLING_CODES = setOf(401, 403)
        const val MAX_LOG_ENTRIES = 80
    }
}
