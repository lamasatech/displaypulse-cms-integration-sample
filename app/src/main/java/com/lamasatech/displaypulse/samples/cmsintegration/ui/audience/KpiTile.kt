package com.lamasatech.displaypulse.samples.cmsintegration.ui.audience

/**
 * One figure on the dashboard grid.
 *
 * @property label short caption shown under the value.
 * @property value text to display; a dash when the figure is unavailable.
 */
data class KpiTile(val label: String, val value: String)
