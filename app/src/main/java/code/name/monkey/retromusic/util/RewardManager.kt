package code.name.monkey.retromusic.util

import android.content.Context
import android.os.Handler
import android.os.Looper
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.rest.music.model.RewardData
import java.text.SimpleDateFormat

object RewardManager {

    var rewardData: RewardData = RewardData()
    var startTime = 0L
    var playerPlaying = false
    var curPlayingTime = computePlayingTime(App.getContext())
    val handler = Handler(Looper.getMainLooper())
    private val runnable = Runnable {
        getPlayingTime()
    }


    private val callbackList = arrayListOf<RewardCallback>()

    fun getAvailableTimeByIndex(context: Context, index: Int): Long {
        return PreferenceUtil.getInstance(context).getFreePrizesAvailableTime(index)
    }

    fun setPlayingState(context: Context, startTime: Long, playing: Boolean) {
        playerPlaying = playing
        this@RewardManager.startTime = startTime
        if (!playing) {
            val currentTimeMillis = System.currentTimeMillis()
            val duration = (currentTimeMillis - startTime) / 1000
            val dateAndTime = PreferenceUtil.getInstance(context).playingTime
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val today = simpleDateFormat.format(currentTimeMillis)
            if (todayData(currentTimeMillis, dateAndTime)) {
                val playingSeconds = dateAndTime.split("&")[1].toLong() + duration
                println("RewardManager: ${"$today&$playingSeconds"}")
                PreferenceUtil.getInstance(context).playingTime = "$today&$playingSeconds"
            } else {
                println("RewardManager: ${"$today&$duration"}")
                PreferenceUtil.getInstance(context).playingTime = "$today&$duration"
            }
            handler.removeCallbacks(runnable)
        } else {
            handler.postDelayed(runnable, Constants.HALF_MINUTE_MS)
        }
    }

    private fun computePlayingTime(context: Context): Long {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndTime = PreferenceUtil.getInstance(context).playingTime
        return if (playerPlaying) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val today = simpleDateFormat.format(currentTimeMillis)
            val duration = (currentTimeMillis - startTime) / 1000
            val playingTime: Long
            playingTime = if (todayData(currentTimeMillis, dateAndTime)) {
                dateAndTime.split("&")[1].toLong() + duration
            } else {
                duration
            }
            println("RewardManager: ${"$today&$playingTime"}")
            PreferenceUtil.getInstance(context).playingTime = "$today&$playingTime"
            startTime = currentTimeMillis
            playingTime
        } else {
            if (todayData(currentTimeMillis, dateAndTime)) {
                dateAndTime.split("&")[1].toLong()
            } else 0
        }
    }

    fun playingTimeFinish(): Boolean {
        return curPlayingTime > 1800
    }

    private fun todayData(currentTimeMillis: Long, dateAndTime: String): Boolean {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = simpleDateFormat.format(currentTimeMillis)
        if (dateAndTime.isNotEmpty()) {
            val strings = dateAndTime.split("&")
            if (today == strings[0]) {
                return true
            }
        }
        return false
    }

    fun todayClaimed(context: Context): Boolean {
        val claimLatestDate = PreferenceUtil.getInstance(context).claimLatestDate
        return if (claimLatestDate.isNotEmpty()) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val today = simpleDateFormat.format(System.currentTimeMillis())
            today == claimLatestDate
        } else {
            false
        }
    }

    fun updateClaimDate(context: Context) {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = simpleDateFormat.format(System.currentTimeMillis())
        PreferenceUtil.getInstance(context).claimLatestDate = today
    }

    fun todayTaskClaimed(context: Context, type: Int): Boolean {
        val claimLatestDate = PreferenceUtil.getInstance(context).getClaimTaskLatestDate(type)
        return if (claimLatestDate.isNotEmpty()) {
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
            val today = simpleDateFormat.format(System.currentTimeMillis())
            today == claimLatestDate
        } else {
            false
        }
    }

    private fun getPlayingTime() {
        curPlayingTime = computePlayingTime(App.getContext())
        callbackList.forEach { list ->
            list.updatePlayingTime(curPlayingTime)
        }
        if (playingTimeFinish()) {
            handler.removeCallbacks(runnable)
        } else {
            handler.postDelayed(runnable, Constants.HALF_MINUTE_MS)
        }
    }

    fun addCallback(rewardCallback: RewardCallback) {
        callbackList.add(rewardCallback)
    }

    fun removeCallback(rewardCallback: RewardCallback) {
        callbackList.remove(rewardCallback)
    }

    fun addDownloadCount(context: Context) {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).downloadCount
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = simpleDateFormat.format(currentTimeMillis)
        val count = getTodayCount(dateAndCount)
        PreferenceUtil.getInstance(context).downloadCount = "$today&$count"
        callbackList.forEach { callback ->
            callback.updateDownloadCount(count)
        }
    }

    fun getDownloadCount(context: Context): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).downloadCount
        return if (todayData(currentTimeMillis, dateAndCount)) {
            dateAndCount.split("&")[1].toInt()
        } else {
            0
        }
    }

    fun addSearchCount(context: Context) {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).searchCount
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = simpleDateFormat.format(currentTimeMillis)
        val count = getTodayCount(dateAndCount)
        PreferenceUtil.getInstance(context).searchCount = "$today&$count"
        callbackList.forEach { callback ->
            callback.updateSearchCount(count)
        }
    }

    fun getSearchCount(context: Context): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).searchCount
        return if (todayData(currentTimeMillis, dateAndCount)) {
            dateAndCount.split("&")[1].toInt()
        } else {
            0
        }
    }

    fun addPlayCount(context: Context) {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).playCount
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val today = simpleDateFormat.format(currentTimeMillis)
        val count = getTodayCount(dateAndCount)
        PreferenceUtil.getInstance(context).playCount = "$today&$count"
        callbackList.forEach { callback ->
            callback.updatePlayCount(count)
        }
    }

    fun getPlayCount(context: Context): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).playCount
        return if (todayData(currentTimeMillis, dateAndCount)) {
            dateAndCount.split("&")[1].toInt()
        } else {
            0
        }
    }

    fun getSpinCount(context: Context): Int {
        val currentTimeMillis = System.currentTimeMillis()
        val dateAndCount = PreferenceUtil.getInstance(context).spinCount
        return if (todayData(currentTimeMillis, dateAndCount)) {
            dateAndCount.split("&")[1].toInt()
        } else {
            0
        }
    }

    private fun getTodayCount(dateAndCount: String): Int {
        val currentTimeMillis = System.currentTimeMillis()
        return if (todayData(currentTimeMillis, dateAndCount)) {
            val strings = dateAndCount.split("&")
            strings[1].toInt() + 1
        } else {
            1
        }
    }

    interface RewardCallback {
        fun updatePlayingTime(playingTime: Long)
        fun updateDownloadCount(downloadCount: Int)
        fun updateSearchCount(searchCount: Int)
        fun updatePlayCount(playCount: Int)
        fun updateSpinCount(spinCount: Int)
    }
}