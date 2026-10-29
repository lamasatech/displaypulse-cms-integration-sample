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
     * @return the new campaign.
     */
    fun create(name: String): Campaign = Campaign(
        id = ids.newId(),
        name = name.trim().ifEmpty { DEFAULT_NAME },
        contentId = ids.newId(),
    )

    private companion object {
        const val DEFAULT_NAME = "Sample campaign"
    }
}
