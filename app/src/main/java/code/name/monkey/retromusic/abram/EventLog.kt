/*
 * Copyright (c) 2020 Free Music Team.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package code.name.monkey.retromusic.abram


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object EventLog {
    private val TAG = javaClass.simpleName

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var preferences: SharedPreferences

    fun init(context: Context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
    }

    fun log(action: String) {
        Log.d(TAG, "logEvent:$action")
        log(action, null)
    }

    fun log(action: String, bundle: Bundle?) {
        Log.d(TAG, "logEvent:$action, bundle:$bundle")
        try {
            firebaseAnalytics.logEvent(action, bundle)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}
