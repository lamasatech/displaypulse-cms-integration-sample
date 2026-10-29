package com.lamasatech.displaypulse.samples.cmsintegration.domain.model

/**
 * A play that the device has confirmed as started and that has not been closed yet.
 *
 * It only exists after the device acknowledged the matching `started` event, so it always
 * reflects what the device knows, not what the app merely intends.
 *
 * @property playId unique id of this play; every distinct play gets its own id.
 * @property campaignId id of the campaign the play belongs to.
 * @property contentId id of the content item being played.
 * @property startedAtMs start time in Unix time milliseconds, as claimed by this app.
 */
data class ActivePlay(
    val playId: String,
    val campaignId: String,
    val contentId: String,
    val startedAtMs: Long,
)
