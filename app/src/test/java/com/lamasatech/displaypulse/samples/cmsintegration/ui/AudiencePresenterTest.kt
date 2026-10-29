package com.lamasatech.displaypulse.samples.cmsintegration.ui

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import com.lamasatech.displaypulse.samples.cmsintegration.ui.audience.AudiencePresenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests the display text for measurements, including figures the device does not provide. */
class AudiencePresenterTest {

    private val campaign = Campaign("camp-1", "Demo", "content-1")

    private fun snapshot(
        activeCampaignId: String? = null,
        ots: Int? = null,
        gender: Map<String, Int> = emptyMap(),
        age: Map<String, Int> = emptyMap(),
    ) = AudienceSnapshot(
        measurementStatus = "measured", observedAt = "2026-10-28T10:00:00Z",
        peopleNow = 3, lookingNow = 2, gender = gender, age = age,
        viewersTotal = null, avgDwellMs = 4_250, reachTotal = null, otsTotal = ots, frequency = 1.5,
        verifiedReachTotal = null, staffExcludedTotal = null, activeCampaignId = activeCampaignId,
    )

    private fun kpi(view: com.lamasatech.displaypulse.samples.cmsintegration.ui.audience.AudienceView, label: String) =
        view.kpis.first { it.label == label }.value

    @Test
    fun `fields the device did not provide show a dash and never zero`() {
        val view = AudiencePresenter.present(snapshot(ots = null), campaign)
        assertEquals(AudiencePresenter.MISSING, kpi(view, "OTS"))
        assertEquals(AudiencePresenter.MISSING, kpi(view, "REACH"))
    }

    @Test
    fun `a measured zero stays zero`() {
        val view = AudiencePresenter.present(snapshot(ots = 0), campaign)
        assertEquals("0", kpi(view, "OTS"))
    }

    @Test
    fun `formats dwell and frequency`() {
        val view = AudiencePresenter.present(snapshot(), campaign)
        assertEquals("4.3s", kpi(view, "AVG DWELL"))
        assertEquals("1.50", kpi(view, "FREQUENCY"))
    }

    @Test
    fun `no snapshot yet`() {
        val view = AudiencePresenter.present(null, campaign)
        assertEquals("No data yet", view.status)
        assertTrue(view.kpis.all { it.value == AudiencePresenter.MISSING })
        assertTrue(view.campaignLink.contains("waiting"))
    }

    @Test
    fun `demographics line`() {
        val view = AudiencePresenter.present(
            snapshot(gender = mapOf("male" to 2, "female" to 1), age = mapOf("adult" to 3)), campaign,
        )
        assertEquals("Gender M:2 F:1 ?:0   Age MN:0 AD:3 MA:0 SR:0 ?:0", view.demographics)
    }

    @Test
    fun `demographics not provided show as unavailable`() {
        val view = AudiencePresenter.present(snapshot(), campaign)
        assertTrue(view.demographics.contains("not available"))
    }

    @Test
    fun `campaign link is shown only when the device reports this campaign`() {
        assertTrue(AudiencePresenter.present(snapshot("camp-1"), campaign).campaignLink.contains("✔"))
        assertTrue(AudiencePresenter.present(snapshot("other"), campaign).campaignLink.contains("another campaign"))
        assertTrue(AudiencePresenter.present(snapshot(null), campaign).campaignLink.contains("no play"))
    }

    @Test
    fun `after a polling error the last figures stay but are labelled, and the campaign link is not claimed`() {
        val view = AudiencePresenter.present(snapshot("camp-1", ots = 5), campaign, error = "timeout")
        assertEquals("Error: timeout · figures below are the last ones received", view.status)
        assertEquals("5", kpi(view, "OTS"))
        assertFalse(view.campaignLink.contains("✔"))
        assertTrue(view.campaignLink.contains("unknown"))
    }

    @Test
    fun `a polling error before any data shows only the error`() {
        val view = AudiencePresenter.present(null, campaign, error = "HTTP 403 tier_not_entitled")
        assertEquals("Error: HTTP 403 tier_not_entitled", view.status)
    }
}
