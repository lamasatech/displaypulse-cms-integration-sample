package com.lamasatech.displaypulse.samples.cmsintegration.data.store

/** Persistent storage for the [ConnectionSettings] entered by the user. */
interface ConnectionSettingsStore {

    /** @return the saved settings, or defaults when nothing has been saved yet. */
    fun load(): ConnectionSettings

    /**
     * Saves [settings], replacing any previous ones. They are used from now on even when the token
     * cannot be stored.
     *
     * @param settings the settings to keep.
     * @return `false` when the token could not be stored securely, so it is lost when the app closes.
     */
    fun save(settings: ConnectionSettings): Boolean
}
