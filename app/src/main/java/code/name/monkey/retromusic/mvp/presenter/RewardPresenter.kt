package code.name.monkey.retromusic.mvp.presenter

import android.content.Context
import android.widget.Toast
import code.name.monkey.retromusic.Result
import code.name.monkey.retromusic.model.TaskData
import code.name.monkey.retromusic.mvp.BaseView
import code.name.monkey.retromusic.mvp.Presenter
import code.name.monkey.retromusic.mvp.PresenterImpl
import code.name.monkey.retromusic.providers.interfaces.Repository
import code.name.monkey.retromusic.rest.music.model.Prize
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

interface PrizeView : BaseView {
    fun showFreePrize(index: Int, availableTime: Long, prize: Prize)

    fun showCheckInPrize(checkInDay: Int, checkInDone: Boolean, checkInPrizes: List<Prize>)

    fun showScore(score: Int)

    fun showPuzzle(pieces: List<Int>, expireTime: String)

    fun obtainPieces(newPieces: List<Int>, collectedPieces: List<Int>)

    fun spinLottery(result: Int, spinTime: Int)

    fun showPlayingTime(time: Long)

    fun updateSpin(spinTime: Int)

    fun showDownloadCount(count: Int)

    fun showSearchCount(count: Int)

    fun showPlayCount(count: Int)

    fun showTasks(tasks: List<TaskData>)

    fun updateTaskClaimed(type: Int)

    fun updateAllTaskClaimed()

    fun showCashToWin(cash: Int, index: Int)

    fun showCash(cash: Int)

    fun initFinish()
}

interface RewardPresenter : Presenter<PrizeView> {

    fun startReward()

    fun getFreePrize()

    fun winReward(prize: Prize, index: Int, multiple: Int, type: Int, card: String = "")

    fun getCheckInData()

    fun getPuzzleCheckInData(card: String)

    fun getSpinData(card: String)

    fun startSpin(card: String, index: Int = -1)

    fun getPlayingTime()

    fun buyReward(action: Int, card: String, address: String = "")

    fun getFreeData()

    class RewardPresenterImpl @Inject constructor(
        val context: Context,
        val repository: Repository
    ) : PresenterImpl<PrizeView>(), RewardPresenter, CoroutineScope {

        private val job = Job()
        private val taskCallback = object : RewardManager.RewardCallback {
            override fun updatePlayingTime(playingTime: Long) {
                view.showPlayingTime(playingTime)
            }

            override fun updateDownloadCount(downloadCount: Int) {
                view.showDownloadCount(downloadCount)
            }

            override fun updateSearchCount(searchCount: Int) {
                view.showSearchCount(searchCount)
            }

            override fun updatePlayCount(playCount: Int) {
                view.showPlayCount(playCount)
            }

            override fun updateSpinCount(spinCount: Int) {

            }
        }

        override val coroutineContext: CoroutineContext
            get() = Dispatchers.IO + job

        override fun getFreePrize() {
            launch {
                RewardManager.rewardData?.let { data ->
                    withContext(Dispatchers.Main) {
                        view?.showScore(data.score)
                        view?.showCash(data.cash)
                    }
                    if (data.freePrizes.isNotEmpty()) {
                        for (index in 0..5) {
                            val availableTime =
                                RewardManager.getAvailableTimeByIndex(context, index)
                            val prize = data.freePrizes[index]
                            withContext(Dispatchers.Main) {
                                view?.showFreePrize(index, availableTime, prize)
                            }
                        }
                    }
                }
            }
        }

        override fun winReward(prize: Prize, index: Int, multiple: Int, type: Int, card: String) {
        }


        override fun getCheckInData() {
        }

        override fun getPuzzleCheckInData(card: String) {
        }

        override fun getSpinData(card: String) {
        }

        override fun startSpin(card: String, index: Int) {
        }

        override fun getPlayingTime() {
            val seconds = RewardManager.curPlayingTime
            view.showPlayingTime(seconds)
        }

        override fun buyReward(action: Int, card: String, address: String) {
        }

        override fun getFreeData() {
            getFreePrize()
            getTaskData()
        }

        private fun getTaskData() {
            getPlayingTime()
            getDownloadCount()
            getSearchCount()
            getPlayCount()
            RewardManager.addCallback(taskCallback)
        }

        private fun getPlayCount() {
            val playCount = RewardManager.getPlayCount(context)
            view.showPlayCount(playCount)
        }

        private fun getSearchCount() {
            val searchCount = RewardManager.getSearchCount(context)
            view.showSearchCount(searchCount)
        }

        private fun getDownloadCount() {
            val downloadCount = RewardManager.getDownloadCount(context)
            view.showDownloadCount(downloadCount)
        }

        override fun detachView() {
            super.detachView()
            RewardManager.removeCallback(taskCallback)
            job.cancel()
        }

        override fun startReward() {
            launch {
                when (val response = repository.getRewardInfo()) {
                    is Result.Success -> {
                        RewardManager.rewardData = response.data
                    }
                    is Result.Error -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Network unavailable, please check it and try again later!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    getFreeData()
                    view.initFinish()
                }
            }
        }
    }
}