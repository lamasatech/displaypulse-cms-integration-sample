package com.lamasatech.displaypulse.samples.cmsintegration.data

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.lamasatech.displaypulse.samples.cmsintegration.data.api.CmsApiFactory
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.domain.FixedClock
import com.lamasatech.displaypulse.samples.cmsintegration.domain.InMemorySettingsStore
import com.lamasatech.displaypulse.samples.cmsintegration.domain.PlayEventFactory
import com.lamasatech.displaypulse.samples.cmsintegration.domain.SequentialIds
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ActivePlay
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.ApiResult
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayEventType
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlayReceipt
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Exercises the real HTTP stack (Retrofit, Gson, OkHttp) against a fake device running locally. */
class CmsRepositoryImplTest {

    private lateinit var server: MockWebServer
    private lateinit var settings: InMemorySettingsStore
    private lateinit var repository: CmsRepositoryImpl

    private val factory = PlayEventFactory(FixedClock(1_700_000_000_000L), SequentialIds())
    private val campaign = Campaign("camp-uuid", "Demo", "content-uuid")

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        settings = InMemorySettingsStore(ConnectionSettings(server.hostName, server.port, "secret-token"))
        val gson = Gson()
        repository = CmsRepositoryImpl(CmsApiFactory.create(settings, gson), gson)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `play event posts the campaign id with a bearer token to the configured host`() = runTest {
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"status":"accepted","play_id":"id-2","receipt_id":41}"""))
        val event = factory.started(campaign)

        val result = repository.sendPlayEvent(event)

        assertEquals(ApiResult.Success(PlayReceipt(receiptId = 41, duplicate = false)), result)
        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/play-events", request.path)
        assertEquals("Bearer secret-token", request.getHeader("Authorization"))
        assertTrue(request.getHeader("Content-Type")!!.startsWith("application/json"))
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals("1.0", body["schema_version"].asString)
        assertEquals("started", body["event_type"].asString)
        assertEquals("camp-uuid", body["campaign_id"].asString)
        assertEquals("content-uuid", body["content_id"].asString)
        assertEquals(event.eventId, body["event_id"].asString)
        assertEquals("2023-11-14T22:13:20.000Z", body["occurred_at"].asString)
        // The device rejects unknown fields, so absent optionals must not be serialized at all.
        assertFalse(body.has("reason"))
    }

    @Test
    fun `terminal event omits campaign and content ids and carries the reason`() = runTest {
        server.enqueue(MockResponse().setResponseCode(202).setBody("""{"status":"accepted","play_id":"p","receipt_id":1}"""))
        val started = factory.started(campaign)
        val play = ActivePlay(started.playId, "camp-uuid", "content-uuid", 0)

        repository.sendPlayEvent(factory.terminal(play, PlayEventType.FAILED, "player_error"))

        val body = JsonParser.parseString(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("failed", body["event_type"].asString)
        assertEquals(started.playId, body["play_id"].asString)
        assertEquals("player_error", body["reason"].asString)
        assertFalse(body.has("campaign_id"))
        assertFalse(body.has("content_id"))
    }

    @Test
    fun `200 means the device saw an identical retry`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"duplicate","play_id":"p","receipt_id":9}"""))
        val result = repository.sendPlayEvent(factory.started(campaign))
        assertEquals(ApiResult.Success(PlayReceipt(receiptId = 9, duplicate = true)), result)
    }

    @Test
    fun `429 exposes the advertised retry delay`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(429).setHeader("Retry-After", "3")
                .setBody("""{"error":"rate_limited","message":"slow down","retry_after_ms":2500}"""),
        )
        val result = repository.sendPlayEvent(factory.started(campaign)) as ApiResult.HttpError
        assertEquals(429, result.code)
        assertEquals(2_500L, result.retryAfterMs)
    }

    @Test
    fun `retry-after header is used when the body has no delay`() = runTest {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "4").setBody("{}"))
        val result = repository.sendPlayEvent(factory.started(campaign)) as ApiResult.HttpError
        assertEquals(4_000L, result.retryAfterMs)
    }

    @Test
    fun `conflict and bad request surface the device error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(409).setBody("""{"error":"event_id_conflict","message":"reused"}"""))
        val result = repository.sendPlayEvent(factory.started(campaign)) as ApiResult.HttpError
        assertEquals(409, result.code)
        assertEquals("event_id_conflict", result.error)
        assertNull(result.retryAfterMs)
    }

    @Test
    fun `an error body that is not JSON does not crash`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503).setBody("<html>down</html>"))
        val result = repository.sendPlayEvent(factory.started(campaign)) as ApiResult.HttpError
        assertEquals(503, result.code)
        assertNull(result.error)
    }

    @Test
    fun `measurement snapshot maps partial json and the active campaign`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"schema_version":2,"measurement_status":"measured","observed_at":"2026-10-28T10:00:00Z",
                "current":{"people_now":3,"looking_now":2,"demographics_now":{
                  "gender":{"male":2,"female":1,"unknown":0},"age":{"adult":3}}},
                "session":{"viewers_total":5,"avg_dwell_ms":4250,"reach_total":4,"ots_total":9,"frequency":1.5},
                "active_campaign_id":"camp-uuid"}""",
            ),
        )
        val snapshot = (repository.audienceCurrent() as ApiResult.Success).value
        assertEquals("measured", snapshot.measurementStatus)
        assertEquals(3, snapshot.peopleNow)
        assertEquals(mapOf("male" to 2, "female" to 1, "unknown" to 0), snapshot.gender)
        assertEquals(9, snapshot.otsTotal)
        assertNull("absent in the response, so absent in the snapshot", snapshot.verifiedReachTotal)
        assertEquals("camp-uuid", snapshot.activeCampaignId)
        assertEquals("/audience/current", server.takeRequest().path)
    }

    @Test
    fun `stale snapshot has null current and no counts`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """{"schema_version":2,"measurement_status":"stale","observed_at":null,"current":null,"active_campaign_id":null}""",
            ),
        )
        val snapshot = (repository.audienceCurrent() as ApiResult.Success).value
        assertEquals("stale", snapshot.measurementStatus)
        assertNull(snapshot.peopleNow)
        assertTrue(snapshot.gender.isEmpty())
    }

    @Test
    fun `health check works without an access token`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"ok"}"""))
        val result = repository.health()
        assertTrue(result is ApiResult.Success)
        assertEquals("/health", server.takeRequest().path)
    }

    @Test
    fun `missing token sends no authorization header`() = runTest {
        settings.settings = settings.settings.copy(token = "")
        server.enqueue(MockResponse().setResponseCode(401).setBody("""{"error":"unauthorized"}"""))
        val result = repository.audienceCurrent() as ApiResult.HttpError
        assertEquals(401, result.code)
        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `host changes apply to the next request without rebuilding the client`() = runTest {
        val other = MockWebServer().apply { start() }
        try {
            other.enqueue(MockResponse().setResponseCode(200).setBody("""{"status":"ok"}"""))
            settings.settings = settings.settings.copy(host = other.hostName, port = other.port)
            repository.health()
            assertEquals(1, other.requestCount)
            assertEquals(0, server.requestCount)
        } finally {
            other.shutdown()
        }
    }

    @Test
    fun `unconfigured host is a network error and sends nothing`() = runTest {
        settings.settings = ConnectionSettings()
        val result = repository.health()
        assertTrue(result is ApiResult.NetworkError)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `unreachable device is a network error`() = runTest {
        server.shutdown()
        val result = repository.audienceCurrent()
        assertTrue(result is ApiResult.NetworkError)
    }

    @Test
    fun `malformed success body is a network error, not a crash`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("not json"))
        assertTrue(repository.audienceCurrent() is ApiResult.NetworkError)
    }
}
