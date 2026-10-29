package com.lamasatech.displaypulse.samples.cmsintegration.data.store

import android.content.Context
import android.content.SharedPreferences

/** Opens the single preferences file shared by this app's stores. */
internal object SamplePreferences {
    private const val NAME = "cms_sample"

    /**
     * @param context any context; the preferences belong to the application.
     * @return the app's private preferences file.
     */
    fun open(context: Context): SharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
}
