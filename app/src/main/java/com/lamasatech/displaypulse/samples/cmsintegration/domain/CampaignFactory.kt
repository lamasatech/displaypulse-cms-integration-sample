package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Campaign
import javax.inject.Inject

/**
 * Creates new [Campaign] instances with freshly generated ids.
 *
 * @property ids source of unique ids.
 */
class CampaignFactory @Inject constructor(private val ids: IdGenerator) {

    /**
     * Creates a campaign with a new id and a new content id.
     *
     * @param name display name; surrounding whitespace is trimmed and a blank name becomes a default.
     * @param advertiser the brand behind the campaign in [com.lamasatech.displaypulse.samples.cmsintegration.domain.model.Advertiser.normalize]
     * form, or `null` for none.
     * @return the new campaign.
     */
    fun create(name: String, advertiser: String? = null): Campaign = Campaign(
        id = ids.newId(),
        name = name.trim().ifEmpty { DEFAULT_NAME },
        contentId = ids.newId(),
        advertiser = advertiser,
    )

    private companion object {
        const val DEFAULT_NAME = "Sample campaign"
    }
}
