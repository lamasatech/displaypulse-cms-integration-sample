package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.PlaybackStore
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState

/**
 * Test [PlaybackStore] that keeps its record in memory. Sharing one instance between two view
 * models simulates an app restart.
 *
 * @property state the stored record; `null` simulates a record that cannot be read.
 * @property writable whether [save] succeeds; set to `false` to simulate a full or failing disk.
 * @property saves number of successful writes.
 */
class InMemoryPlaybackStore(
    var state: PlaybackState? = PlaybackState(),
    var writable: Boolean = true,
) : PlaybackStore {
    var saves = 0

    override fun load() = state

    override suspend fun save(state: PlaybackState): Boolean {
        if (!writable) return false
        this.state = state
        saves++
        return true
    }
}
