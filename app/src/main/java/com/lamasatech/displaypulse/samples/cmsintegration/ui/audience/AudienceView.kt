package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

/**
 * Ready-to-display text for the live measurements section.
 *
 * @property status one-line description of the data's freshness.
 * @property kpis the figures shown in the grid, in display order.
 * @property demographics one line summarising the gender and age counts.
 * @property campaignLink whether the measurements are attributed to the current campaign.
 */
data class AudienceView(
    val status: String,
    val kpis: List<KpiTile>,
    val demographics: String,
    val campaignLink: String,
)
