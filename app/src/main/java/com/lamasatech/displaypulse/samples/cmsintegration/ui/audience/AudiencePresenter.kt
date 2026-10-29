package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.AudienceSnapshot
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import java.util.Locale

/**
 * Turns an [AudienceSnapshot] into display text.
 *
 * A figure the device did not provide is shown as a dash and never as 0, because "not provided"
 * and "measured zero" mean different things.
 */
object AudiencePresenter {
    /** Text shown in place of a figure that is not available. */
    const val MISSING = "—"

    /**
     * @param snapshot the latest measurements, or `null` before any have arrived.
     * @param campaign the app's current campaign, used to tell whether the measurements belong to it.
     * @param error why the last poll failed, or `null` while polling works. When set, [snapshot] is
     * the last data received and is labelled as such.
     * @return the text to display.
     */
    fun present(snapshot: AudienceSnapshot?, campaign: Campaign?, error: String? = null): AudienceView = AudienceView(
        status = if (error == null) status(snapshot) else errorStatus(error, snapshot),
        kpis = kpis(snapshot),
        demographics = demographics(snapshot),
        campaignLink = if (error != null && snapshot != null) STALE_CAMPAIGN_LINK else campaignLink(snapshot, campaign),
    )

    private const val STALE_CAMPAIGN_LINK = "Campaign link: unknown while live data is failing"

    private fun errorStatus(error: String, snapshot: AudienceSnapshot?): String =
        if (snapshot == null) "Error: $error" else "Error: $error · figures below are the last ones received"

    private fun status(snapshot: AudienceSnapshot?): String = when {
        snapshot == null -> "No data yet"
        snapshot.observedAt == null -> snapshot.measurementStatus
        else -> "${snapshot.measurementStatus} · observed ${snapshot.observedAt}"
    }

    private fun kpis(s: AudienceSnapshot?): List<KpiTile> = listOf(
        KpiTile("OTS", display(s?.otsTotal)),
        KpiTile("REACH", display(s?.reachTotal)),
        KpiTile("VIEWERS", display(s?.viewersTotal)),
        KpiTile("PEOPLE NOW", display(s?.peopleNow)),
        KpiTile("LOOKING NOW", display(s?.lookingNow)),
        KpiTile("AVG DWELL", s?.avgDwellMs?.let { String.format(Locale.ROOT, "%.1fs", it / 1000.0) } ?: MISSING),
        KpiTile("FREQUENCY", s?.frequency?.let { String.format(Locale.ROOT, "%.2f", it) } ?: MISSING),
        KpiTile("VERIFIED REACH", display(s?.verifiedReachTotal)),
        KpiTile("STAFF EXCL.", display(s?.staffExcludedTotal)),
    )

    private fun demographics(s: AudienceSnapshot?): String {
        if (s == null || (s.gender.isEmpty() && s.age.isEmpty())) {
            return "Demographics: $MISSING (not available)"
        }
        val gender = listOf("M" to "male", "F" to "female", "?" to "unknown").joinToString(" ") { (tag, key) ->
            "$tag:${s.gender[key] ?: 0}"
        }
        val age = listOf("MN" to "minor", "AD" to "adult", "MA" to "mature_adult", "SR" to "senior", "?" to "unknown")
            .joinToString(" ") { (tag, key) -> "$tag:${s.age[key] ?: 0}" }
        return "Gender $gender   Age $age"
    }

    private fun campaignLink(s: AudienceSnapshot?, campaign: Campaign?): String = when {
        s == null -> "Campaign link: waiting for data"
        s.activeCampaignId == null -> "Campaign link: no play of this connection is on screen"
        s.activeCampaignId == campaign?.id -> "Campaign link: ✔ audience is attributed to this campaign"
        else -> "Campaign link: another campaign is active (${s.activeCampaignId})"
    }

    private fun display(value: Int?): String = value?.toString() ?: MISSING
}
