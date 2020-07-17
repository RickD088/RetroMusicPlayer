package code.name.monkey.retromusic.mvp

import code.name.monkey.retromusic.model.TaskData
import code.name.monkey.retromusic.mvp.presenter.PrizeView
import code.name.monkey.retromusic.rest.music.model.Prize

open class SimplePrizeView : PrizeView {
    override fun showFreePrize(index: Int, availableTime: Long, prize: Prize) {

    }

    override fun showCheckInPrize(checkInDay: Int, checkInDone: Boolean, checkInPrizes: List<Prize>) {
    }

    override fun showScore(score: Int) {
    }

    override fun showPuzzle(pieces: List<Int>, expireTime: String) {
    }

    override fun obtainPieces(newPieces: List<Int>, collectedPieces: List<Int>) {

    }

    override fun spinLottery(result: Int, spinTime: Int) {

    }

    override fun showPlayingTime(time: Long) {

    }

    override fun updateSpin(spinTime: Int) {

    }

    override fun showDownloadCount(count: Int) {

    }

    override fun showSearchCount(count: Int) {

    }

    override fun showPlayCount(count: Int) {

    }

    override fun showTasks(tasks: List<TaskData>) {

    }

    override fun updateTaskClaimed(type: Int) {

    }

    override fun updateAllTaskClaimed() {

    }

    override fun showCashToWin(cash: Int, index: Int) {

    }

    override fun showCash(cash: Int) {

    }

    override fun initFinish() {

    }

    override fun showEmptyView() {
    }
}