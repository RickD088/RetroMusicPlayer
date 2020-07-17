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

package code.name.monkey.retromusic.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.activities.base.AbsSlidingMusicPanelActivity

class CheckInActivity : AbsSlidingMusicPanelActivity() {

    private val TAG = javaClass.simpleName

    override fun createContentView(): View {
        return wrapSlidingMusicPanel(R.layout.activity_main_content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setDrawUnderStatusBar()
        super.onCreate(savedInstanceState)
        setStatusbarColor(resources.getColor(R.color.color_bg_gift_game))
        setNavigationbarColorAuto()
        setLightNavigationBar(true)
        rewardPresenter.startReward()

        Log.d(
                TAG,
                "${Constants.STATS_REWARD_CHECK_IN}_${Constants.STATS_REWARD_SCORE}_${Constants.STATS_ENTER}"
        )
        EventLog.log("${Constants.STATS_REWARD_CHECK_IN}_${Constants.STATS_REWARD_SCORE}_${Constants.STATS_ENTER}")
    }

    override fun onDestroy() {
        super.onDestroy()
        rewardPresenter.detachView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}