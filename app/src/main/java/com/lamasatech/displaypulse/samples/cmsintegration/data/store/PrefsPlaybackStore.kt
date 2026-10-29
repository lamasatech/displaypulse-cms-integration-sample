package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import android.annotation.SuppressLint
import android.content.Context
import com.lamasatech.displaypulse.samples.cmsintegration.domain.model.PlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * [PlaybackStore] backed by the app's private preferences, as one JSON record.
 *
 * Writes use `commit()` rather than `apply()`: `apply()` returns before the data reaches the disk,
 * so an event could be sent and then lost if the app is killed straight after.
 *
 * @property codec converts the state to and from JSON.
 */
@Singleton
class PrefsPlaybackStore @Inject constructor(
    @ApplicationContext context: Context,
    private val codec: PlaybackStateCodec,
) : PlaybackStore {
    private val prefs = SamplePreferences.open(context)

    override fun load(): PlaybackState? {
        val json = prefs.getString(KEY_PLAYBACK, null) ?: return PlaybackState()
        return codec.decode(json)
    }

    @SuppressLint("UseKtx") // the KTX edit {} discards commit()'s result, which says whether the write succeeded
    override suspend fun save(state: PlaybackState): Boolean = withContext(Dispatchers.IO) {
        prefs.edit().putString(KEY_PLAYBACK, codec.encode(state)).commit()
    }

    private companion object {
        const val KEY_PLAYBACK = "playback"
    }
}
