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
 * @property advertiser the brand behind the campaign, already in [Advertiser.normalize] form, or
 * `null` when none was given. It is sent only to a device that accepts it.
 * @throws IllegalArgumentException if [advertiser] is not in normalised form.
 */
data class Campaign(val id: String, val name: String, val contentId: String, val advertiser: String? = null) {
    init {
        require(advertiser == null || Advertiser.isNormalized(advertiser)) { "advertiser is not a valid advertiser" }
    }
}
