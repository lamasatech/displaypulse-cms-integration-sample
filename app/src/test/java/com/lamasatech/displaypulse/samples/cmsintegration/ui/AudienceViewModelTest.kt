package com.lamasatech.displaypulse.samples.cmsintegration.ui

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.CampaignFactory
import com.lamasatech.displaypulse.samples.cmsintegration.domain.DeliveryOutcomeClassifier
import com.lamasatech.displaypulse.samples.cmsintegration.domain.FakeCmsRepository
import com.lamasatech.displaypulse.samples.cmsintegration.domain.FixedClock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.InMemoryPlaybackStore
import com.lamasatech.displaypulse.samples.cmsintegration.domain.InMemorySettingsStore
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlayEventFactory
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlayEventSender
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlaybackReducer
import com.lamasatech.displaypulse.samples.cmsintegration.domain.SequentialIds
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEvent
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import com.lamasatech.displaypulse.samples.cmsintegration.ui.audience.AudienceViewModel
import com.lamasatech.displaypulse.samples.cmsintegration.ui.audience.ConnectionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Tests the main screen's view model: saving the connection, health checks, play delivery, resuming
 * after a restart and polling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudienceViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val clock = FixedClock()
    private val ids = SequentialIds()
    private val repository = FakeCmsRepository()
    private val settings = InMemorySettingsStore(ConnectionSettings("127.0.0.1", 8400, "good-token"))
    private val playbacks = InMemoryPlaybackStore(PlaybackState(campaign = Campaign("camp-1", "Demo", "content-1")))

    private val accepted = ApiResult.Success(PlayReceipt(receiptId = 1, duplicate = false))
    private val storageUnavailable = ApiResult.HttpError(503, "storage_unavailable", null, null)
    private val reachable = 10L to ApiResult.Success("""{"status":"ok"}""")

    private fun viewModel() = AudienceViewModel(
        settingsStore = settings,
        playbackStore = playbacks,
        campaignFactory = CampaignFactory(ids),
        repository = repository,
        sender = PlayEventSender(repository),
        outcomes = DeliveryOutcomeClassifier(),
        reducer = PlaybackReducer(PlayEventFactory(clock, ids)),
        clock = clock,
    )

    private fun AudienceViewModel.lastLog() = state.value.log.first()

    @Test
    fun `a host other than this device is refused and not saved`() = runTest(main.dispatcher) {
        // No health reply is queued, so a check of the refused host would fail the test.
        for (host in listOf("", "192.168.1.10", "example.com", "http://127.0.0.1", "127.0.0.1:8400", "a\$b;c|d<e>")) {
            val vm = viewModel()

            vm.saveConnection(host, "8400", "good-token")
            runCurrent()

            assertEquals(host, "127.0.0.1", settings.settings.host)
            assertEquals(host, ConnectionSettings.HOST_REQUIREMENT, vm.lastLog().message)
        }
    }

    @Test
    fun `localhost in any case and with spaces is saved in lower case (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += reachable

        vm.saveConnection("  LocalHost ", "8400", "good-token")
        advanceTimeBy(1_000)

        assertEquals("localhost", settings.settings.host)
        assertTrue(vm.state.value.connection is ConnectionStatus.Online)
    }

    @Test
    fun `a token that cannot be sent in a header is refused and not saved`() = runTest(main.dispatcher) {
        val vm = viewModel()

        vm.saveConnection("127.0.0.1", "8400", "dpcms_abc def")
        runCurrent()

        assertEquals("good-token", settings.settings.token)
        assertTrue(vm.lastLog().isError)
        assertTrue(vm.lastLog().message.contains("cannot be sent"))
    }

    @Test
    fun `a valid token is saved and checked (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += reachable

        vm.saveConnection("127.0.0.1", "8400", "  dpcms_new-Token_1  ")
        advanceTimeBy(1_000)

        assertEquals("dpcms_new-Token_1", settings.settings.token)
        assertTrue(vm.state.value.connection is ConnectionStatus.Online)
    }

    @Test
    fun `a token that cannot be stored securely is reported and still used`() = runTest(main.dispatcher) {
        val vm = viewModel()
        settings.tokenStorable = false
        repository.healthReplies += reachable

        vm.saveConnection("127.0.0.1", "8400", "dpcms_new-Token_1")
        advanceTimeBy(1_000)

        assertEquals("dpcms_new-Token_1", vm.state.value.settings.token)
        assertTrue(vm.state.value.log.any { it.isError && it.message.contains("could not be stored securely") })
    }

    @Test
    fun `a stored token is not reported (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += reachable

        vm.saveConnection("127.0.0.1", "8400", "dpcms_new-Token_1")
        advanceTimeBy(1_000)

        assertFalse(vm.state.value.log.any { it.message.contains("could not be stored securely") })
    }

    @Test
    fun `a slow answer about an old address does not overwrite a newer check`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += 3_000L to ApiResult.NetworkError("timeout")
        repository.healthReplies += reachable

        vm.saveConnection("localhost", "8400", "good-token")
        runCurrent()
        vm.saveConnection("127.0.0.1", "8400", "good-token")
        advanceTimeBy(5_000)

        assertTrue(vm.state.value.connection is ConnectionStatus.Online)
    }

    @Test
    fun `a single slow failing check is still reported (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += 3_000L to ApiResult.NetworkError("timeout")

        vm.checkHealth()
        advanceTimeBy(5_000)

        assertEquals(ConnectionStatus.Offline("timeout"), vm.state.value.connection)
    }

    @Test
    fun `the device address cannot change while a play is open but the token can`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.playReplies += accepted
        vm.startPlay("")
        runCurrent()
        assertNotNull(vm.state.value.playback.activePlay)

        vm.saveConnection("localhost", "8400", "good-token")
        runCurrent()
        assertEquals("127.0.0.1", settings.settings.host)
        assertTrue(vm.lastLog().message.contains("before changing the device address"))

        repository.healthReplies += reachable
        vm.saveConnection("127.0.0.1", "8400", "fixed-token")
        advanceTimeBy(1_000)
        assertEquals("fixed-token", settings.settings.token)
    }

    @Test
    fun `the device address can change when no play is open (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.healthReplies += reachable

        vm.saveConnection("localhost", "8400", "good-token")
        advanceTimeBy(1_000)

        assertEquals("localhost", settings.settings.host)
    }

    @Test
    fun `a closing event stuck on 503 can be discarded so a new play can start`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.playReplies += listOf(accepted, storageUnavailable, storageUnavailable)
        vm.startPlay("")
        runCurrent()
        vm.endPlay(PlayEventType.COMPLETED, null)
        runCurrent()
        vm.retryPending()
        runCurrent()

        val stuck = vm.state.value.playback
        assertNotNull(stuck.pending)
        assertFalse(stuck.canStart)
        assertFalse(stuck.canEnd)

        vm.discardPending()
        runCurrent()

        val playback = vm.state.value.playback
        assertNull(playback.pending)
        assertNull(playback.activePlay)
        assertTrue(playback.canStart)
        assertEquals(3, repository.sentEvents.size) // the discard sends nothing
    }

    @Test
    fun `a discarded start leaves no play open`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.playReplies += storageUnavailable
        vm.startPlay("")
        runCurrent()

        vm.discardPending()
        runCurrent()

        assertNull(vm.state.value.playback.activePlay)
        assertTrue(vm.state.value.playback.canStart)
    }

    @Test
    fun `a typed content duration is sent with the start in milliseconds`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.playReplies += accepted

        vm.startPlay(" 600 ")
        runCurrent()

        assertEquals(600_000L, repository.sentEvents.single().expectedDurationMs)
    }

    @Test
    fun `an invalid content duration sends nothing and says why`() = runTest(main.dispatcher) {
        val vm = viewModel()

        listOf("4", "7201", "1.5", "ten").forEach { text ->
            vm.startPlay(text)
            runCurrent()
            assertTrue(text, vm.lastLog().message.contains("Content duration must be"))
        }

        assertTrue(repository.sentEvents.isEmpty())
        assertNull(vm.state.value.playback.pending)
        // Control: a blank duration starts the play without one.
        repository.playReplies += accepted
        vm.startPlay("  ")
        runCurrent()
        assertNull(repository.sentEvents.single().expectedDurationMs)
    }

    @Test
    fun `a pending event is on disk before it is sent`() = runTest(main.dispatcher) {
        val vm = viewModel()
        var savedWhenSent: PlayEvent? = null
        repository.onPlayEvent = { savedWhenSent = playbacks.state?.pending }
        repository.playReplies += accepted

        vm.startPlay("")
        runCurrent()

        assertEquals(repository.sentEvents.single(), savedWhenSent)
        assertNull(playbacks.state?.pending) // the reply is saved too
        assertEquals(vm.state.value.playback.activePlay, playbacks.state?.activePlay)
    }

    @Test
    fun `after a restart the pending start is resent with the same ids`() = runTest(main.dispatcher) {
        repository.playReplies += storageUnavailable
        viewModel().startPlay("")
        runCurrent()
        val first = repository.sentEvents.single()

        repository.playReplies += ApiResult.Success(PlayReceipt(receiptId = 1, duplicate = true))
        val restarted = viewModel()
        runCurrent()

        assertEquals(first, repository.sentEvents[1])
        assertEquals(first.playId, restarted.state.value.playback.activePlay?.playId)
        assertNull(restarted.state.value.playback.pending)
    }

    @Test
    fun `after a restart the open play can still be closed with its own play id`() = runTest(main.dispatcher) {
        repository.playReplies += accepted
        viewModel().startPlay("")
        runCurrent()
        val playId = repository.sentEvents.single().playId

        repository.playReplies += accepted
        val restarted = viewModel()
        runCurrent()
        assertEquals(1, repository.sentEvents.size) // nothing was pending, so nothing is resent
        restarted.endPlay(PlayEventType.COMPLETED, null)
        runCurrent()

        assertEquals(PlayEventType.COMPLETED, repository.sentEvents[1].type)
        assertEquals(playId, repository.sentEvents[1].playId)
        assertNull(restarted.state.value.playback.activePlay)
    }

    @Test
    fun `a pending closing event is resent with the same ids after a restart`() = runTest(main.dispatcher) {
        repository.playReplies += listOf(accepted, storageUnavailable)
        val vm = viewModel()
        vm.startPlay("")
        runCurrent()
        vm.endPlay(PlayEventType.INTERRUPTED, "user_skipped")
        runCurrent()
        val closing = repository.sentEvents[1]

        repository.playReplies += accepted
        val restarted = viewModel()
        runCurrent()

        assertEquals(closing, repository.sentEvents[2])
        assertNull(restarted.state.value.playback.activePlay)
        assertTrue(restarted.state.value.playback.canStart)
    }

    @Test
    fun `a discarded event is not resent after a restart`() = runTest(main.dispatcher) {
        repository.playReplies += storageUnavailable
        val vm = viewModel()
        vm.startPlay("")
        runCurrent()
        vm.discardPending()
        runCurrent()

        viewModel()
        runCurrent()

        assertEquals(1, repository.sentEvents.size)
    }

    @Test
    fun `an unreadable saved state is reported and the app starts empty`() = runTest(main.dispatcher) {
        playbacks.state = null

        val vm = viewModel()
        runCurrent()

        assertEquals(PlaybackState(), vm.state.value.playback)
        assertTrue(vm.lastLog().isError)
        assertTrue(vm.lastLog().message.contains("could not be read"))
        assertTrue(repository.sentEvents.isEmpty())
    }

    @Test
    fun `a failed save is reported but the event is still sent`() = runTest(main.dispatcher) {
        playbacks.writable = false
        repository.playReplies += accepted
        val vm = viewModel()

        vm.startPlay("")
        runCurrent()

        assertEquals(1, repository.sentEvents.size)
        assertTrue(vm.state.value.log.any { it.isError && it.message.startsWith("Could not save the play state") })
    }

    @Test
    fun `a new campaign is saved`() = runTest(main.dispatcher) {
        val vm = viewModel()

        vm.createCampaign("Spring")
        runCurrent()

        assertEquals("Spring", playbacks.state?.campaign?.name)
        assertEquals(vm.state.value.playback.campaign, playbacks.state?.campaign)
    }

    @Test
    fun `polling stops on 403 instead of repeating it`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.audienceReply = ApiResult.HttpError(403, "tier_not_entitled", null, null)

        whilePolling(vm) {
            advanceTimeBy(10_000)

            assertEquals(1, repository.audienceCalls)
            assertFalse(vm.state.value.polling)
            assertEquals("HTTP 403 tier_not_entitled", vm.state.value.audienceError)
            assertTrue(vm.lastLog().message.startsWith("Live updates stopped"))
        }
    }

    @Test
    fun `polling keeps going after a network error (control)`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.audienceReply = ApiResult.NetworkError("timeout")

        whilePolling(vm) {
            advanceTimeBy(2_500)

            assertEquals(3, repository.audienceCalls) // at 0, 1.2 s and 2.4 s
            assertTrue(vm.state.value.polling)
        }
    }

    @Test
    fun `a 429 poll waits as long as the device asks`() = runTest(main.dispatcher) {
        val vm = viewModel()
        repository.audienceReply = ApiResult.HttpError(429, "rate_limited", null, 5_000)

        whilePolling(vm) {
            advanceTimeBy(4_900)
            assertEquals(1, repository.audienceCalls)

            advanceTimeBy(200)
            assertEquals(2, repository.audienceCalls)
            assertNull(vm.state.value.audienceError) // a pause request is not an error
        }
    }

    /**
     * Starts polling, runs [checks], then always stops polling: a poll loop left running would keep
     * the test scheduler busy forever, so a failed check would hang instead of failing.
     */
    private fun TestScope.whilePolling(vm: AudienceViewModel, checks: TestScope.() -> Unit) {
        vm.onScreenVisible(true)
        vm.setPolling(true)
        runCurrent()
        try {
            checks()
        } finally {
            vm.onScreenVisible(false)
        }
    }
}
