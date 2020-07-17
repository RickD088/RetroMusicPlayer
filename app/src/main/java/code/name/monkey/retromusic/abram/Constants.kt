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

import code.name.monkey.retromusic.BuildConfig
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Constants {

    // build 相关
    val DEBUG = BuildConfig.DEBUG
    const val VERSION_NAME = BuildConfig.VERSION_NAME
    const val APPLICATION_ID = BuildConfig.APPLICATION_ID

    // api 相关
    const val API_URL = "http://api.yourdomain.com/"
    const val PLAYLIST_SINGER = "singerFM"
    const val INIT_TIME_KEY = "INIT_TIME_KEY"
    const val MIN_SUPPORT_VER_KEY = "MIN_SUPPORT_VER_KEY"
    const val ID_PER_DEVICE_KEY = "ID_PER_DEVICE_KEY"
    const val ID_PER_USER_KEY = "ID_PER_USER_KEY"
    const val EMAIL_PER_USER_KEY = "EMAIL_PER_USER_KEY"
    const val AVATAR_PER_USER_KEY = "AVATAR_PER_USER_KEY"
    const val REGISTERED_USER_KEY = "REGISTERED_USER_KEY"
    const val DATA_SYNC_USER_KEY = "DATA_SYNC_USER_KEY"
    const val TRY_AUTO_SYNC_USER_KEY = "TRY_AUTO_SYNC_USER_KEY"
    const val PUSH_TOKEN_KEY = "PUSH_TOKEN_KEY"
    const val SEPARATOR_SEARCH_KEYWORDS = "|:-:|"
    const val MIN_SEARCH_KEYWORD_LENGTH = 2
    val SEARCH_HOT_KEYWORDS = arrayListOf<String>()
    val SEARCH_HISTORY_RECENT = arrayListOf<String>()
    const val PAGE_SIZE = 20

    // firebase 相关
    const val CFG_LANGUAGE = "lang"
    const val CFG_LOCATION = "locale"
    const val CFG_RATING_APP = "bool_ask_rating"
    const val CFG_RATING_APP_INSTALL_HOURS = "int_rating_app_install_hours"
    const val CFG_RATING_APP_FULL_PLAYED = "int_rating_app_full_played"
    const val CFG_REWARD_RANDOM_FACTOR = "int_reward_random_factor"

    // ui 相关
    const val UI_THEME = "light"
    const val UI_DOWNLOAD_PLAYLIST_ID = Long.MAX_VALUE - 100
    const val UI_FAVORITE_PLAYLIST_ID = Long.MAX_VALUE - 1000
    const val UI_DEFAULT_USERNAME = "Music Lover"

    // 统计相关
    const val STATS_SUCCESS = "success"
    const val STATS_FAIL = "fail"
    const val STATS_CONFIG = "fetch_config"
    const val STATS_USER = "user"
    const val STATS_USER_LOGIN = "login"
    const val STATS_USER_LOGOUT = "logout"
    const val STATS_MUSIC_PLAY = "music_play"
    const val STATS_MUSIC_COMPLETE = "music_complete"
    const val STATS_MUSIC_ERROR = "music_error_"
    const val STATS_MUSIC_START = "music_start"
    const val STATS_MUSIC_PAUSE = "music_pause"
    const val STATS_MUSIC_SEEK = "music_seek"
    const val STATS_MUSIC_SEARCH = "music_search"
    const val STATS_MUSIC_DOWNLOAD = "music_download"
    const val STATS_NO_RESULT = "no_result"
    const val STATS_ADS = "ads"
    const val STATS_SHOW = "show"
    const val STATS_CLICK = "click"
    const val STATS_ENTER = "enter"
    const val STATS_CLOSE = "close"
    const val STATS_REWARD = "reward"
    const val STATS_REWARD_BUBBLE = "bubble"
    const val STATS_REWARD_CUP = "cup"
    const val STATS_REWARD_NOT_CD = "not_cd"
    const val STATS_REWARD_CHECK_IN = "check_in"
    const val STATS_REWARD_CASH = "cash"
    const val STATS_REWARD_SCORE = "score"
    const val STATS_REWARD_PUZZLE = "puzzle"
    const val STATS_REWARD_SPIN = "spin"
    const val STATS_REWARD_CLAIM = "claim"
    const val STATS_REWARD_DOUBLE = "double"

    // 时间相关
    const val ONE_HOUR_SEC = 3600L //1小时
    const val ONE_HOUR_MS = 3600000L //1小时
    const val THREE_HOURS_MS = ONE_HOUR_MS * 3L //3小时
    const val SIX_HOURS_MS = ONE_HOUR_MS * 6L //6小时
    const val HALF_DAY_MS = ONE_HOUR_MS * 12L //半天
    const val ONE_DAY_MS = ONE_HOUR_MS * 24L //1天
    const val TEN_DAYS_MS = ONE_DAY_MS * 10L //10天
    const val HALF_MINUTE_MS = 30000L //半分钟
    const val ONE_MINUTE_MS = 60000L //1分钟
    const val THREE_MINUTE_MS = 180000L //3分钟
    const val TEN_MINUTES_MS = 600000L //10分钟
    const val ONE_MINUTE_NS = 60000000000L //1分钟
    const val HALF_HOUR_MS = 1800000L //半小时
    const val ONE_FIFTH_SECOND_MS = 200L //1/5秒
    const val ONE_SECOND_MS = 1000L //1秒
    const val THREE_SECONDS_MS = 3000L //3秒
    const val FIVE_SECONDS_MS = 5000L //5秒
    const val TEN_SECONDS_MS = 10000L //10秒
    const val PLAYING_TIME_SEC = 1800   //当天播放时间 30分钟

    // 播放＆下载相关
    var PLAY_MUSIC_CACHE: SimpleCache? = null
    const val PLAY_FULL_COUNT_KEY = "PLAY_FULL_COUNT_KEY"
    const val PLAY_ON_NONE_NETWORK = 0
    const val PLAY_ON_MOBILE_NETWORK = 1
    const val PLAY_ON_WIFI_NETWORK = 1
    const val DOWNLOAD_READABLE_NAME = true
    const val DOWNLOAD_ASSUMED_COMPLETION_PERCENTAGE = 96
    const val DOWNLOAD_NEW_ITEM_COUNT_KEY = "DOWNLOAD_NEW_ITEM_COUNT_KEY"
    val DOWNLOAD_INPROGRESS_QUEUE = arrayListOf<String>()
    val DOWNLOAD_THREAD_POOL_EXECUTOR = ThreadPoolExecutor(0, 1,
            300L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()
    )

    //游戏积分相关
    const val SCORE_SPIN = 20
    const val SCORE_CASH = 2000
    const val AMAZON = "amazon"
    const val GOOGLE = "google"
    const val PAYPAL = "paypal"
    const val WALMART = "walmart"
    const val STARBUCKS = "starbucks"
    const val TARGET = "target"
    const val CASH = "cash"
}