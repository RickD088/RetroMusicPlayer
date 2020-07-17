package code.name.monkey.retromusic.rest.music.model

import android.content.Intent
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.service.MusicService
import java.time.LocalDateTime

data class RewardData(
        val checkinDay: Int = 0,
        var checkinDone: Boolean = false,
        val checkinPrizes: List<Prize> = listOf(
            Prize(1, 10, 1, 0, 31, "", "Day1", 0),
            Prize(1, 20, 1, 0, 32, "", "Day2", 0),
            Prize(1, 30, 1, 0, 33, "", "Day3", 0),
            Prize(1, 40, 1, 0, 34, "", "Day4", 0),
            Prize(1, 50, 1, 0, 35, "", "Day5", 0),
            Prize(1, 60, 1, 0, 36, "", "Day7", 0),
            Prize(1, 70, 1, 0, 37, "", "Day6", 0)
        ),
        val puzzleCheckinDay: PuzzleInt = PuzzleInt(),
        var puzzleCheckinDone: PuzzleCheckinDone = PuzzleCheckinDone(),
        val puzzleCheckinPrizes: List<Prize> = listOf(
            Prize(2, 1, 5, 0, 31, "puzzle1.png", "Day1", 0),
            Prize(2, 0, 5, 0, 32, "", "Day2", 0),
            Prize(2, 0, 5, 0, 33, "", "Day3", 0),
            Prize(2, 2, 5, 0, 34, "puzzle2.png", "Day4", 0),
            Prize(2, 0, 5, 0, 35, "", "Day5", 0),
            Prize(2, 0, 5, 0, 36, "", "Day7", 0),
            Prize(2, 3, 5, 0, 37, "puzzle3.png", "Day6", 0)
        ),
        val puzzleExpireTime: PuzzleExpireTime = PuzzleExpireTime(),
        val freePrizes: List<Prize> = listOf(
            Prize(1, 5, 2, 1000, 15, "", "+5", 0),
            Prize(1, 0, 2, 3000, 11, "", "?", 0),
            Prize(1, 10, 2, 2000, 13, "", "+10", 0),
            Prize(1, 10, 2, 2000, 13, "", "+10", 0),
            Prize(1, 0, 2, 3000, 16, "", "?", 0),
            Prize(1, 50, 2, 43200, 8, "cup.png", "Cup", 1)
        ),
        val pieces: Pieces = Pieces(),
        val newPieces: List<Int> = listOf(),
        val needRefresh: Boolean = false,
        var score: Int = 88,
        var cash: Int = 66,
        val cashToWin: Int = 0,
        var spin: PuzzleInt = PuzzleInt(),
        val spinResult: Int = -1,
        val spinPrizes: List<Prize> = listOf(
            Prize(1, 5, 4, 0, 3, "", "+5", 0),
            Prize(1, 0, 4, 0, 2, "", "?", 0),
            Prize(1, 5, 4, 0, 1, "", "+5", 0),
            Prize(1, 0, 4, 0, 4, "", "Sorry", 0),
            Prize(1, 0, 4, 0, 4, "", "?", 0),
            Prize(1, 10, 4, 0, 5, "", "+10", 0),
            Prize(5, 1, 4, 0, 6, "", "Puzzlex1", 0),
            Prize(1, 15, 4, 0, 8, "", "+15", 0)
        ),
        val spinUserCount: Int = 3,
        val taskPrizes: List<Prize> = listOf(
            Prize(1, 30, 3, 0, 19, "", "Listen 30 min music", 1),
            Prize(1, 10, 3, 0, 20, "", "Download 3 songs", 2),
            Prize(1, 10, 3, 0, 21, "", "Do searching", 3),
            Prize(1, 10, 3, 0, 22, "", "Spin the lucky wheel 10 times", 4)
        ),
        val wonUsers: List<WonUser> = listOf(),
        val cashOutThreshold: Int = 2000
) {

    fun getPuzzleExpireTimeByCard(card: String): String {
        val expireTime = when (card) {
            Constants.GOOGLE -> puzzleExpireTime.google
            Constants.PAYPAL -> puzzleExpireTime.paypal
            Constants.WALMART -> puzzleExpireTime.walmart
            Constants.STARBUCKS -> puzzleExpireTime.starbucks
            Constants.TARGET -> puzzleExpireTime.target
            else -> puzzleExpireTime.amazon
        }
        return expireTime.split("T")[0]
    }

    fun getPuzzleSpinByCard(card: String): Int {
        return when (card) {
            Constants.GOOGLE -> spin.google
            Constants.PAYPAL -> spin.paypal
            Constants.WALMART -> spin.walmart
            Constants.STARBUCKS -> spin.starbucks
            Constants.TARGET -> spin.target
            else -> spin.amazon
        }
    }

    fun setPuzzleSpinByCard(card: String, count: Int) {
        when (card) {
            Constants.GOOGLE -> spin.google = count
            Constants.PAYPAL -> spin.paypal = count
            Constants.WALMART -> spin.walmart = count
            Constants.STARBUCKS -> spin.starbucks = count
            Constants.TARGET -> spin.target = count
            else -> spin.amazon = count
        }
    }

    fun getPuzzleCheckinDayByCard(card: String): Int {
        return when (card) {
            Constants.GOOGLE -> puzzleCheckinDay.google
            Constants.PAYPAL -> puzzleCheckinDay.paypal
            Constants.WALMART -> puzzleCheckinDay.walmart
            Constants.STARBUCKS -> puzzleCheckinDay.starbucks
            Constants.TARGET -> puzzleCheckinDay.target
            else -> puzzleCheckinDay.amazon
        }
    }

    fun getPuzzleCheckinDoneByCard(card: String): Boolean {
        return when (card) {
            Constants.GOOGLE -> puzzleCheckinDone.google
            Constants.PAYPAL -> puzzleCheckinDone.paypal
            Constants.WALMART -> puzzleCheckinDone.walmart
            Constants.STARBUCKS -> puzzleCheckinDone.starbucks
            Constants.TARGET -> puzzleCheckinDone.target
            else -> puzzleCheckinDone.amazon
        }
    }

    fun setPuzzleCheckinDoneByCard(card: String, done: Boolean) {
        when (card) {
            Constants.GOOGLE -> puzzleCheckinDone.google = done
            Constants.PAYPAL -> puzzleCheckinDone.paypal = done
            Constants.WALMART -> puzzleCheckinDone.walmart = done
            Constants.STARBUCKS -> puzzleCheckinDone.starbucks = done
            Constants.TARGET -> puzzleCheckinDone.target = done
            else -> puzzleCheckinDone.amazon = done
        }
    }

    fun getPiecesByCard(card: String): List<Int> {
        return when (card) {
            Constants.GOOGLE -> pieces.google
            Constants.PAYPAL -> pieces.paypal
            Constants.WALMART -> pieces.walmart
            Constants.STARBUCKS -> pieces.starbucks
            Constants.TARGET -> pieces.target
            else -> pieces.amazon
        }
    }

    fun setPiecesByCard(card: String, collectedList: List<Int>) {
        when (card) {
            Constants.GOOGLE -> {
                pieces.google.clear()
                pieces.google.addAll(collectedList)
            }
            Constants.PAYPAL -> {
                pieces.paypal.clear()
                pieces.paypal.addAll(collectedList)
            }
            Constants.WALMART -> {
                pieces.walmart.clear()
                pieces.walmart.addAll(collectedList)
            }
            Constants.STARBUCKS -> {
                pieces.starbucks.clear()
                pieces.starbucks.addAll(collectedList)
            }
            Constants.TARGET -> {
                pieces.target.clear()
                pieces.target.addAll(collectedList)
            }
            else -> {
                pieces.amazon.clear()
                pieces.amazon.addAll(collectedList)
            }
        }
        App.getContext().sendBroadcast(Intent(MusicService.REWARD_PIECE_CHANGED))
    }
}

data class PuzzleExpireTime(
    val amazon: String = LocalDateTime.now().toString(),
    val google: String = amazon,
    val paypal: String = amazon,
    val walmart: String = amazon,
    val starbucks: String = amazon,
    val target: String = amazon
)

data class PuzzleCheckinDone(
        var amazon: Boolean = false,
        var google: Boolean = false,
        var paypal: Boolean = false,
        var walmart: Boolean = false,
        var starbucks: Boolean = false,
        var target: Boolean = false
)

data class PuzzleInt(
        var amazon: Int = 1,
        var google: Int = 1,
        var paypal: Int = 1,
        var walmart: Int = 1,
        var starbucks: Int = 1,
        var target: Int = 1
)

data class Pieces(
    val amazon: MutableList<Int> = mutableListOf(),
    val google: MutableList<Int> = mutableListOf(),
    val paypal: MutableList<Int> = mutableListOf(),
    val walmart: MutableList<Int> = mutableListOf(),
    val starbucks: MutableList<Int> = mutableListOf(),
    val target: MutableList<Int> = mutableListOf()
)

data class Prize(
        val action: Int,
        val amount: Int,
        val category: Int,
        val cooldown: Int,
        val id: Int,
        val img: String,
        val name: String,
        val type: Int
) {
    companion object {
        const val ACTION_SCORE = 1
        const val ACTION_PIECE = 2
        const val ACTION_GIFT_CARD = 3
        const val ACTION_GIFT = 4
        const val ACTION_CASH = 5

        const val TYPE_LISTEN = 1
        const val TYPE_DOWNLOAD = 2
        const val TYPE_SEARCH = 3
        const val TYPE_SPIN = 4
        const val TYPE_VIDEO = 5
    }
}

data class WonUser(
        val name: String,
        val avatar: String
)