package com.lamasatech.displaypulse.samples.cmsintegration

import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * The only activity. It hosts the navigation graph declared in `activity_main`, and every screen
 * of the app is a fragment shown inside it.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity(R.layout.activity_main)
