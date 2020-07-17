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
import android.preference.PreferenceManager
import android.util.Log
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.BuildConfig
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.rest.music.model.ConfigInfo
import code.name.monkey.retromusic.util.PlaylistsUtil
import code.name.monkey.retromusic.util.PreferenceUtil
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import java.util.*


object RemoteConfig {

    private val TAG = javaClass.simpleName
    private val defaultConfig = hashMapOf(
        Constants.CFG_LANGUAGE to "",
        Constants.CFG_LOCATION to "",
        Constants.CFG_RATING_APP to false,
        Constants.CFG_RATING_APP_INSTALL_HOURS to 1,
        Constants.CFG_RATING_APP_FULL_PLAYED to 0,
        Constants.CFG_REWARD_RANDOM_FACTOR to 10
    )
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var preferenceUtil: PreferenceUtil
    private lateinit var preferences: SharedPreferences
    private var svt = -1f
    private var ip = ""
    private var utc:Date? = null

    fun init(context: Context) {
        preferenceUtil = PreferenceUtil.getInstance(context)
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        initFirebaseRemoteConfig()
        changeDefaultSettings()
    }

    fun updateApiSettings(setting: ConfigInfo) {
        ip = setting.ip
        svt = setting.svt
        utc = if (setting.utc > 0) Date(setting.utc) else Date()
        setInitTime(utc)

        setString(Constants.MIN_SUPPORT_VER_KEY, setting.minSupVer)
    }

    private fun initFirebaseRemoteConfig() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaultConfig)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    EventLog.log("${Constants.STATS_CONFIG}_${Constants.STATS_SUCCESS}")
                } else {
                    EventLog.log("${Constants.STATS_CONFIG}_${Constants.STATS_FAIL}")
                }
            }
    }

    private fun changeDefaultSettings() {
        // 首次使用，改变默认值
        val editor: SharedPreferences.Editor = preferences.edit()
        if (!preferenceUtil.introShown()) {
            preferenceUtil.setIntroShown()
            preferenceUtil.setGeneralTheme(Constants.UI_THEME)
            preferenceUtil.albumGridStyle = R.layout.item_image_gradient
            preferenceUtil.setAlbumGridSize(2)
            preferenceUtil.setArtistGridSize(1)

            editor.putBoolean(PreferenceUtil.ADAPTIVE_COLOR_APP, true)
            editor.putBoolean(PreferenceUtil.TOGGLE_VOLUME, true)
            editor.putBoolean(PreferenceUtil.CAROUSEL_EFFECT, true)

            PlaylistsUtil.createPlaylist(App.getContext(),
                App.getContext().getString(R.string.favorites))
        }
        editor.putBoolean(PreferenceUtil.TOGGLE_HOME_BANNER, false)
        editor.putInt(PreferenceUtil.LAST_PAGE, R.id.action_gift)
        editor.apply()
    }

    fun setString(key: String, value: String) {
        if (key.isNotBlank()) {
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putString(key, value)
            editor.apply()
        }
    }

    /**
     * 获取 shared preferences
     */
    fun getString(key: String, defValue: String): String {
        return if (key.isNullOrBlank()) defValue else preferences.getString(key, defValue) ?: defValue
    }

    /**
     * 获取 firebase remote config
     */
    fun getString(key: String): String {
        return remoteConfig.getString(key)
    }

    fun setLong(key: String, value: Long) {
        if (key.isNotBlank()) {
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putLong(key, value)
            editor.apply()
        }
    }

    /**
     * 获取 shared preferences
     */
    fun getLong(key: String, defValue: Long): Long {
        return if (key.isNullOrBlank()) defValue else preferences.getLong(key, defValue)
    }

    /**
     * 获取 firebase remote config
     */
    fun getLong(key: String): Long {
        return remoteConfig.getLong(key)
    }

    fun setBoolean(key: String, value: Boolean) {
        if (key.isNotBlank()) {
            val editor: SharedPreferences.Editor = preferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }
    }

    /**
     * 获取 shared preferences
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return if (key.isNullOrBlank()) defValue else preferences.getBoolean(key, defValue)
    }

    /**
     * 获取 firebase remote config
     */
    fun getBoolean(key: String): Boolean {
        return remoteConfig.getBoolean(key)
    }

    fun getFullPlayedCount(): Long {
        return getLong(Constants.PLAY_FULL_COUNT_KEY, 0)
    }

    fun incFullPlayedCount() {
        Log.d(TAG, Constants.STATS_MUSIC_COMPLETE)
        EventLog.log(Constants.STATS_MUSIC_COMPLETE)
        setLong(Constants.PLAY_FULL_COUNT_KEY, getFullPlayedCount() + 1L)
    }

    fun getEmail(): String {
        return getString(Constants.EMAIL_PER_USER_KEY, "")
    }

    fun setEmail(email: String) {
        setString(Constants.EMAIL_PER_USER_KEY, email)
    }

    fun getAvatar(): String {
        return getString(Constants.AVATAR_PER_USER_KEY, "")
    }

    fun setAvatar(avatar: String) {
        setString(Constants.AVATAR_PER_USER_KEY, avatar)
    }

    fun isLogin(): Boolean {
        return getUid().isNotBlank() && getUid() != getDid()
    }

    fun getUid(): String {
        return getString(Constants.ID_PER_USER_KEY, "")
    }

    fun setUid(uid: String) {
        setString(Constants.ID_PER_USER_KEY, uid)
    }

    fun getDid(): String {
        return getString(Constants.ID_PER_DEVICE_KEY, "")
    }

    fun isUserDataSync(): Boolean {
        return getBoolean(Constants.DATA_SYNC_USER_KEY, false)
    }

    fun setUserDataSync(reg: Boolean) {
        setBoolean(Constants.DATA_SYNC_USER_KEY, reg)
    }

    fun isRegistered(): Boolean {
        return getBoolean(Constants.REGISTERED_USER_KEY, false)
    }

    fun setRegistered(reg: Boolean) {
        setBoolean(Constants.REGISTERED_USER_KEY, reg)
    }

    fun isTryAutoSync(): Boolean {
        return getBoolean(Constants.TRY_AUTO_SYNC_USER_KEY, false)
    }

    fun setTryAutoSync(auto: Boolean) {
        setBoolean(Constants.TRY_AUTO_SYNC_USER_KEY, auto)
    }

    fun getPushToken(): String {
        return getString(Constants.PUSH_TOKEN_KEY, "")
    }

    fun setPushToken(token: String) {
        setString(Constants.PUSH_TOKEN_KEY, token)
    }

    fun getDownloadNewItemCount(): Int {
        return getLong(Constants.DOWNLOAD_NEW_ITEM_COUNT_KEY, 0).toInt()
    }

    fun setDownloadNewItemCount(count: Int) {
        setLong(Constants.DOWNLOAD_NEW_ITEM_COUNT_KEY, count.toLong())
    }

    fun getInitTime(): Long {
        return getLong(Constants.INIT_TIME_KEY, 0)
    }

    private fun setInitTime(now: Date?) {
        if (now != null && getInitTime() <= 0) {
            setLong(Constants.INIT_TIME_KEY, now.time)
        }
    }

    fun getRewardRandomFactor(): Int {
        return getLong(Constants.CFG_REWARD_RANDOM_FACTOR).toInt()
    }

}
