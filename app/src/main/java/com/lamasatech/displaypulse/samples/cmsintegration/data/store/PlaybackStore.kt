package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState

/**
 * Persistent storage for the whole [PlaybackState]: the campaign, the open play and the event not
 * yet acknowledged by the device.
 *
 * They are stored as one record so they can never disagree after a restart. A player that loses its
 * pending event or its open play cannot close that play on the device, so the record is written
 * before each event is sent and after each reply.
 */
interface PlaybackStore {

    /**
     * Reads the saved state. Runs on the calling thread; the record is small.
     *
     * @return the saved state, an empty [PlaybackState] when nothing has been saved yet, or `null`
     * when a record exists but cannot be read (for example it was written by an incompatible
     * version or is damaged).
     */
    fun load(): PlaybackState?

    /**
     * Writes [state] to disk, replacing the previous record, and waits until the write has finished.
     *
     * @param state the state to keep.
     * @return `true` when the record is on disk; `false` when the write failed.
     */
    suspend fun save(state: PlaybackState): Boolean
}
