package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * A group of plays that are reported together.
 *
 * Campaigns are owned and created by this app: the device exposes no "create campaign"
 * operation. The [id] simply travels with every `started` play event, and the device links its
 * measurements for that play to it.
 *
 * @property id globally unique campaign id, a random UUID.
 * @property name display name chosen by the user.
 * @property contentId id of the content item played for this campaign.
 */
data class Campaign(val id: String, val name: String, val contentId: String)
