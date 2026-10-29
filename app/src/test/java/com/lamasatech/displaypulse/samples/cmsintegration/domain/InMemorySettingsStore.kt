package com.lamasatech.displaypulse.samples.cmsintegration.domain

import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettings
import com.lamasatech.displaypulse.samples.cmsintegration.data.store.ConnectionSettingsStore

/**
 * Test [ConnectionSettingsStore] that keeps its value in memory.
 *
 * @property settings the stored settings; assign to simulate the user changing them.
 * @property tokenStorable what [save] reports; set to `false` to simulate a failing key store.
 */
class InMemorySettingsStore(
    var settings: ConnectionSettings = ConnectionSettings(),
    var tokenStorable: Boolean = true,
) : ConnectionSettingsStore {
    override fun load() = settings

    override fun save(settings: ConnectionSettings): Boolean {
        this.settings = settings
        return tokenStorable
    }
}
