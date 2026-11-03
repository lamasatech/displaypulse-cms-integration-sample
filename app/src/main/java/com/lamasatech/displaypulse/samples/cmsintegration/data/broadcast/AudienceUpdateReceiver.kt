package com.lamasatech.displaypulse.samples.cmsintegration.data.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.lamasatech.displaypulse.samples.cmsintegration.domain.AudienceBroadcastSink
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Receives the audience broadcast and hands its payload to the [AudienceBroadcastSink].
 *
 * It is declared in the manifest so that it also receives while the app is in the background.
 * The manifest protects it with a signature permission that only the Audience feature holds, so
 * no other app can send it fake figures.
 */
class AudienceUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != AudienceUpdateContract.ACTION) return
        val payload = intent.getStringExtra(AudienceUpdateContract.EXTRA_PAYLOAD_JSON)
        if (payload == null) Log.w(TAG, "Audience broadcast without ${AudienceUpdateContract.EXTRA_PAYLOAD_JSON}")
        // Resolved per call instead of injected: Hilt field injection in a Kotlin receiver needs a
        // super.onReceive() call that the abstract BroadcastReceiver.onReceive does not allow.
        EntryPointAccessors.fromApplication(context, ReceiverEntryPoint::class.java)
            .audienceBroadcastSink()
            .accept(payload)
    }

    /** Gives the receiver access to the app's singletons. */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {

        /** @return the sink that records broadcasts. */
        fun audienceBroadcastSink(): AudienceBroadcastSink
    }

    private companion object {
        const val TAG = "AudienceUpdateReceiver"
    }
}
