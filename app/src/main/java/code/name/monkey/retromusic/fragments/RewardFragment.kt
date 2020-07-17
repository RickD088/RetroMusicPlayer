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

package code.name.monkey.retromusic.fragments

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import code.name.monkey.retromusic.App
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.abram.RemoteConfig
import code.name.monkey.retromusic.activities.MainActivity
import code.name.monkey.retromusic.dialogs.RewardCashBottomSheetDialogFragment
import code.name.monkey.retromusic.dialogs.RewardDialog
import code.name.monkey.retromusic.fragments.base.AbsMainActivityFragment
import code.name.monkey.retromusic.interfaces.MainActivityFragmentCallbacks
import code.name.monkey.retromusic.mvp.SimplePrizeView
import code.name.monkey.retromusic.mvp.presenter.RewardPresenter
import code.name.monkey.retromusic.rest.music.model.Prize
import code.name.monkey.retromusic.rest.music.model.RewardData
import code.name.monkey.retromusic.service.MusicService
import code.name.monkey.retromusic.util.NavigationUtil
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.android.synthetic.main.activity_main_content.view.*
import kotlinx.android.synthetic.main.fragment_reward.*
import kotlinx.android.synthetic.main.layout_card_puzzle.view.*
import javax.inject.Inject

class RewardFragment : AbsMainActivityFragment(), MainActivityFragmentCallbacks {

    @Inject
    lateinit var rewardPresenter: RewardPresenter

    private val prizeView = PrizeView()
    private val receiver = RewardReceiver()
    private var curCash = 0f
        set(value) {
            field = value
            tv_cash.text = String.format("$%.2f", field)
        }
    private var curScore = 0
        set(value) {
            field = value
            tv_score.text = value.toString()
        }
    private lateinit var cash_layout: LinearLayout
    private lateinit var score_layout: LinearLayout
    private lateinit var tv_cash: TextView
    private lateinit var tv_score: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        App.musicComponent.inject(this)
        return inflater.inflate(R.layout.fragment_reward, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMoneyLayout()
        initCards()
        setOnClickListener()
        startAnimate()
        rewardPresenter.attachView(prizeView)
        rewardPresenter.startReward()
        registerReceiver()
        Log.d(TAG, "${Constants.STATS_REWARD}_${Constants.STATS_ENTER}")
        EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_ENTER}")
    }

    private fun registerReceiver() {
        val filter = IntentFilter()
        filter.addAction(MusicService.REWARD_PIECE_CHANGED)
        filter.addAction(MusicService.REWARD_SCORE_CHANGED)
        filter.addAction(MusicService.REWARD_CHECKIN_CHANGED)
        filter.addAction(MusicService.ACTION_PLAY)
        filter.addAction(MusicService.ACTION_PAUSE)
        filter.addAction(MusicService.ACTION_ERROR)
        requireActivity().registerReceiver(receiver, filter)
    }

    private fun initMoneyLayout() {
        val moneyLayout = (activity as MainActivity).moneyLayout
        cash_layout = moneyLayout.cash_layout
        score_layout = moneyLayout.score_layout
        tv_cash = moneyLayout.tv_cash
        tv_score = moneyLayout.tv_score
    }

    private fun initCards() {
        amazon_layout.iv_card.setImageResource(R.drawable.pic_card_amazon)
        paypal_layout.iv_card.setImageResource(R.drawable.pic_card_paypal)
        starbucks_layout.iv_card.setImageResource(R.drawable.pic_card_starbucks)
        target_layout.iv_card.setImageResource(R.drawable.pic_card_target)
        walmart_layout.iv_card.setImageResource(R.drawable.pic_card_walmart)
        google_layout.iv_card.setImageResource(R.drawable.pic_card_google)
    }

    private fun initCardLayout() {
        RewardManager.rewardData?.let { data ->
            amazon_layout.tv_card.text = getString(R.string.amazon_card_label)
            amazon_layout.tv_price.text = getString(R.string.one_hundred_dollar)
            amazon_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.AMAZON)
            )

            paypal_layout.tv_card.text = getString(R.string.paypal_card_label)
            paypal_layout.tv_price.text = getString(R.string.twenty_dollar)
            paypal_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.PAYPAL)
            )

            starbucks_layout.tv_card.text = getString(R.string.starbucks_card_label)
            starbucks_layout.tv_price.text = getString(R.string.twenty_five_dollar)
            starbucks_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.STARBUCKS)
            )

            target_layout.tv_card.text = getString(R.string.target_card_label)
            target_layout.tv_price.text = getString(R.string.twenty_five_dollar)
            target_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.TARGET)
            )

            walmart_layout.tv_card.text = getString(R.string.walmart_card_label)
            walmart_layout.tv_price.text = getString(R.string.fifty_dollar)
            walmart_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.WALMART)
            )

            google_layout.tv_card.text = getString(R.string.google_card_label)
            google_layout.tv_price.text = getString(R.string.twenty_dollar)
            google_layout.tv_expire.text = String.format(
                getString(R.string.reset_on),
                data.getPuzzleExpireTimeByCard(Constants.GOOGLE)
            )

            checkin_flag.visibility = if (data.checkinDone) View.GONE else View.VISIBLE
            updateRewardView(data)
        }
    }

    private fun updateRewardView(data: RewardData) {
        setCardProgress(data, amazon_layout, Constants.AMAZON)
        setCardProgress(data, google_layout, Constants.GOOGLE)
        setCardProgress(data, paypal_layout, Constants.PAYPAL)
        setCardProgress(data, target_layout, Constants.TARGET)
        setCardProgress(data, walmart_layout, Constants.WALMART)
        setCardProgress(data, starbucks_layout, Constants.STARBUCKS)
    }

    private fun setCardProgress(data: RewardData, cardLayout: View, card: String) {
        val earned = String.format(
            getString(R.string.earned),
            (data.getPiecesByCard(card).size / 16f * 100).toInt()
        )
        val spannableString = SpannableString(earned)
        val start = earned.indexOf(":") + 1
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#FB8A3B")),
            start,
            earned.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            earned.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        cardLayout.tv_progress.text = spannableString
    }

    private fun startAnimate() {
        val animations = arrayListOf<Animator>()
        for (index in 0..5) {
            val animator = ObjectAnimator.ofFloat(getRewardView(index), "translationY", -10f, 10f)
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
            animations.add(animator)
        }

        val animatorSet = AnimatorSet()
        animatorSet.duration = 1500
        animatorSet.playTogether(animations)
        animatorSet.start()

        prizeView.runPlayingTime()
    }

    private fun setOnClickListener() {
        for (index in 0..5) {
            getRewardView(index).setOnClickListener {
                clickRewardView(index)
            }
        }
        btn_ad.setOnClickListener {
            Log.d(
                TAG,
                "${Constants.STATS_REWARD}_${Constants.STATS_ADS}_${Constants.STATS_CLICK}"
            )
            EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_ADS}_${Constants.STATS_CLICK}")
        }
        progress.setOnClickListener {
            clickAvailableRewardView()
        }
        tv_played_label.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        tv_played.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        tv_downloaded_label.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        tv_downloaded.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        tv_searched_label.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        tv_searched.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        score_layout.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        btn_daily.setOnClickListener {
            NavigationUtil.gotoCheckIn(requireActivity())
        }
        paypal_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.PAYPAL)
        }
        paypal_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.PAYPAL)
        }
        amazon_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.AMAZON)
        }
        amazon_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.AMAZON)
        }
        google_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.GOOGLE)
        }
        google_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.GOOGLE)
        }
        starbucks_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.STARBUCKS)
        }
        starbucks_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.STARBUCKS)
        }
        walmart_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.WALMART)
        }
        walmart_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.WALMART)
        }
        target_layout.tv_spin.setOnClickListener {
            NavigationUtil.gotoSpin(requireActivity(), Constants.TARGET)
        }
        target_layout.setOnClickListener {
            NavigationUtil.gotoCollectPrize(requireActivity(), Constants.TARGET)
        }
        cash_layout.setOnClickListener {
            val dialog = RewardCashBottomSheetDialogFragment.create()
            dialog.cashoutCallback = {
                rewardPresenter.buyReward(4, "", it)
            }
            dialog.show(parentFragmentManager, RewardCashBottomSheetDialogFragment.TAG)
            Log.d(
                TAG,
                "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CASH}_${Constants.STATS_CLICK}"
            )
            EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CASH}_${Constants.STATS_CLICK}")
        }
    }

    private fun clickAvailableRewardView() {
        for (index in 0..5) {
            if (index < 5) {
                val duration = RewardManager.getAvailableTimeByIndex(
                    requireContext(),
                    index
                ) - System.currentTimeMillis()
                if (duration < 0) {
                    clickRewardView(index)
                    break
                }
            } else {
                clickRewardView(index)
                break
            }
        }
    }

    private fun clickRewardView(index: Int) {
        if (index < 5) {
            val duration = RewardManager.getAvailableTimeByIndex(
                requireContext(),
                index
            ) - System.currentTimeMillis()
            if (duration > 0) {
                Toast.makeText(
                    context,
                    getString(R.string.refresh_later).format( duration / Constants.ONE_MINUTE_MS),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(
                    TAG,
                    "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_NOT_CD}"
                )
                EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_NOT_CD}")
            } else {
                val prize = getRewardView(index).tag
                if (prize is Prize) {
                    if (prize.action == Prize.ACTION_CASH) {
                        rewardPresenter.startSpin(Constants.CASH, index)

                        Log.d(
                            TAG,
                            "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_CASH}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}"
                        )
                        EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_CASH}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}")
                    } else {
                        val amount =
                            if (prize.amount == 0) (1..3).random() * RemoteConfig.getRewardRandomFactor() / 2 else prize.amount
                        showAdDialog(
                            index,
                            if (prize.amount == 0) prize.copy(amount = amount) else prize
                        )

                        Log.d(
                            TAG,
                            "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_SCORE}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}"
                        )
                        EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_REWARD_SCORE}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}")
                    }
                }
            }
            Log.d(
                TAG,
                "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_CLICK}"
            )
            EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_BUBBLE}_${Constants.STATS_CLICK}")
        } else {
            if (!RewardManager.todayClaimed(requireContext())) {
                if (RewardManager.playingTimeFinish()) {
                    val prize = getRewardView(index).tag
                    if (prize is Prize) {
                        showAdDialog(index, prize.copy(amount = (1..5).random() * RemoteConfig.getRewardRandomFactor()))
                        RewardManager.updateClaimDate(requireContext())
                        Log.d(
                            TAG,
                            "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}"
                        )
                        EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_SUCCESS}")
                    }
                } else {
                    Toast.makeText(
                        context,
                        getString(R.string.to_listen),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(
                        TAG,
                        "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_FAIL}"
                    )
                    EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_CLAIM}_${Constants.STATS_FAIL}")
                }
            } else {
                Toast.makeText(
                    context,
                    getString(R.string.today_claimed),
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(
                    TAG,
                    "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_NOT_CD}"
                )
                EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_REWARD_NOT_CD}")
            }
            Log.d(
                TAG,
                "${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_CLICK}"
            )
            EventLog.log("${Constants.STATS_REWARD}_${Constants.STATS_REWARD_CUP}_${Constants.STATS_CLICK}")
        }
    }

    private fun showAdDialog(index: Int, prize: Prize, cashToWin: Int = 0) {
        val cash = prize.action == Prize.ACTION_CASH
        val dialog = RewardDialog.create(index, if (cash) cashToWin else prize.amount, cash)
        dialog.callback = { position, multiple ->
            rewardPresenter.winReward(prize, index, multiple, 0)
        }
        dialog.show(parentFragmentManager, TAG)
    }

    companion object {

        const val TAG: String = "RewardFragment"

        @JvmStatic
        fun newInstance(): RewardFragment {
            return RewardFragment()
        }
    }

    override fun handleBackPress() = false

    private fun showRewardView(tvReward: TextView, availableTime: Long, prize: Prize) {
        val duration = availableTime - System.currentTimeMillis()
        tvReward.tag = prize
        if (tvReward.id != R.id.tv_reward_6) {
            if (duration > 0) {
                //冷却中
                tvReward.text = getString(R.string.minutes_later).format( duration / Constants.ONE_MINUTE_MS)
                tvReward.textSize = tv_reward_6.textSize / 2
            } else {
                //可领取
                tvReward.text = prize.name
                tvReward.textSize = tv_reward_6.textSize
            }
            tvReward.isSelected = duration <= 0
        } else {
            tvReward.isSelected =
                !RewardManager.todayClaimed(requireContext()) && RewardManager.playingTimeFinish()
        }
    }

    private fun getRewardView(index: Int) = when (index) {
        0 -> tv_reward_1
        1 -> tv_reward_2
        2 -> tv_reward_3
        3 -> tv_reward_4
        4 -> tv_reward_5
        else -> tv_reward_6
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(receiver)
        rewardPresenter.detachView()
    }

    inner class PrizeView : SimplePrizeView() {
        private var time = 0L
        private var lastRefresh = 0L
        private var playing = false
        private val runnable = Runnable {
            // 防止 fragment 销毁后继续运行 runnable
            if (isAdded) {
                // 播放时更新累计播放时长
                if (playing) {
                    showPlayingTime(time + 1)
                }

                // 10秒刷新五个气泡的冷却时间
                if (System.currentTimeMillis() - lastRefresh > Constants.TEN_SECONDS_MS
                    && RewardManager.rewardData != null && RewardManager.rewardData!!.freePrizes.isNotEmpty()
                ) {
                    lastRefresh = System.currentTimeMillis()

                    for (index in 0..5) {
                        val availableTime =
                            RewardManager.getAvailableTimeByIndex(requireContext(), index)
                        val prize = RewardManager.rewardData!!.freePrizes[index]
                        showFreePrize(index, availableTime, prize)
                    }
                }
                runPlayingTime()
            }
        }

        fun runPlayingTime() {
            tv_playing_time?.let {
                it.postDelayed(runnable, Constants.ONE_SECOND_MS)
            }
        }

        fun startPlayingTime() {
            playing = true
        }

        fun stopPlayingTime() {
            playing = false
        }

        @SuppressLint("AnimatorKeep")
        override fun showScore(score: Int) {
            if (tv_score.text.toString().isEmpty()) {
                curScore = score
                tv_score.text = score.toString()
            } else {
                val animator =
                    ObjectAnimator.ofInt(this@RewardFragment, "curScore", curScore, score)
                animator.duration = 1000
                animator.start()
            }
        }

        override fun showFreePrize(index: Int, availableTime: Long, prize: Prize) {
            val tvReward = getRewardView(index)
            showRewardView(tvReward, availableTime, prize)
        }

        override fun showPlayingTime(time: Long) {
            this.time = time
            progress?.let {
                val minutes = time / 60L
                val seconds = time - minutes * 60L
                it.progress = (if (minutes < 30) minutes else 30) / 30f
                tv_playing_time.text = if (seconds < 10) {
                    "$minutes:0$seconds"
                } else {
                    "$minutes:$seconds"
                }
                tv_reward_6.isSelected =
                    !RewardManager.todayClaimed(requireContext()) && RewardManager.playingTimeFinish()
            }
        }

        override fun showDownloadCount(count: Int) {
            tv_downloaded.text = count.toString()
        }

        override fun showSearchCount(count: Int) {
            tv_searched.text = count.toString()
        }

        override fun showPlayCount(count: Int) {
            tv_played.text = count.toString()
        }

        override fun showCashToWin(cash: Int, index: Int) {
            val prize = getRewardView(index).tag
            if (prize is Prize) {
                val rewardPrize = prize.copy(amount = cash)
                showAdDialog(index, rewardPrize, cash)
            }
        }

        @SuppressLint("AnimatorKeep")
        override fun showCash(cash: Int) {
            if (tv_cash.text.toString().isEmpty()) {
                curCash = cash / 100f
                tv_cash.text = String.format("$%.2f", curCash)
            } else {
                val animator =
                    ObjectAnimator.ofFloat(this@RewardFragment, "curCash", curCash, cash / 100f)
                animator.duration = 1000
                animator.start()
            }
        }

        override fun initFinish() {
            initCardLayout()
        }
    }

    // abram
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (!hidden) {
            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun refreshData() {
        initCardLayout()
    }

    inner class RewardReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == MusicService.REWARD_PIECE_CHANGED) {
                updateRewardView(RewardManager.rewardData!!)
            } else if (intent.action == MusicService.REWARD_SCORE_CHANGED) {
                val scoreAnimator = ObjectAnimator.ofInt(
                    this@RewardFragment,
                    "curScore",
                    curScore,
                    RewardManager.rewardData!!.score
                )
                val cashAnimator = ObjectAnimator.ofFloat(
                    this@RewardFragment,
                    "curCash",
                    curCash,
                    RewardManager.rewardData!!.cash / 100f
                )
                val animator = AnimatorSet()
                animator.playTogether(scoreAnimator, cashAnimator)
                animator.duration = 1000
                animator.start()
            } else if (intent.action == MusicService.REWARD_CHECKIN_CHANGED) {
                checkin_flag.visibility = View.GONE
            } else if (intent.action == MusicService.ACTION_PLAY) {
                prizeView?.let { it.startPlayingTime() }
            } else if (intent.action == MusicService.ACTION_PAUSE || intent.action == MusicService.ACTION_ERROR) {
                prizeView?.let { it.stopPlayingTime() }
            }
        }
    }
}