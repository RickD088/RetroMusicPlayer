package code.name.monkey.retromusic.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.abram.Constants
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.util.ViewUtil
import kotlinx.android.synthetic.main.dialog_reward.*
import kotlinx.android.synthetic.main.dialog_reward.btn_ad
import kotlinx.android.synthetic.main.dialog_reward.btn_close
import kotlinx.android.synthetic.main.dialog_reward.iv_bg
import kotlinx.android.synthetic.main.dialog_reward.tv_btn

class RewardDialog : DialogFragment() {

    private val TAG = javaClass.simpleName
    var callback: ((Int, Int) -> Unit)? = null
    var multiple = 1
    var cash = false
    var closeable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFullScreen)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_reward, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        cash = requireArguments().getBoolean("cash")
        closeable = requireArguments().getBoolean("closeable")
        if (cash) {
            tv_cash_label.visibility = View.VISIBLE
            iv_bg.setImageResource(R.drawable.pic_light_green)
            imageView.visibility = View.INVISIBLE
            iv_cash.visibility = View.VISIBLE
            tv_cash.visibility = View.VISIBLE
            tv_cash.text = String.format(getString(R.string.cash_prefix), requireArguments().getInt("score") / 100f)
            textView.text = getString(R.string.deposit_to_balance)
            tv_btn.text = getString(R.string.deposit)
            ViewUtil.startRewardAnimate(iv_cash, iv_bg, btn_ad)
        } else {
            tv_cash_label.visibility = View.GONE
            iv_bg.setImageResource(R.drawable.pic_shine)
            imageView.visibility = View.VISIBLE
            iv_cash.visibility = View.INVISIBLE
            tv_cash.visibility = View.INVISIBLE
            textView.text = getString(R.string.got_gems).format(requireArguments().getInt("score"))
            tv_btn.text = getString(R.string.double_gems).format(requireArguments().getInt("score") * 2)
            ViewUtil.startRewardAnimate(imageView, iv_bg, btn_ad)
        }
        btn_close.setOnClickListener {
            dismissDialog(1)
            Log.d(
                TAG,
                "${Constants.STATS_REWARD_DOUBLE}_${if (cash) Constants.STATS_REWARD_CASH else Constants.STATS_REWARD_SCORE}_${Constants.STATS_CLOSE}"
            )
            EventLog.log("${Constants.STATS_REWARD_DOUBLE}_${if (cash) Constants.STATS_REWARD_CASH else Constants.STATS_REWARD_SCORE}_${Constants.STATS_CLOSE}")
        }
        btn_close.postDelayed({
            ViewUtil.startFadingIn(btn_close)
        }, Constants.ONE_FIFTH_SECOND_MS)
        btn_ad.setOnClickListener {
            dismissDialog(2)
        }
        Log.d(TAG, "${Constants.STATS_REWARD_DOUBLE}_${if (cash) Constants.STATS_REWARD_CASH else Constants.STATS_REWARD_SCORE}_${Constants.STATS_SHOW}")
        EventLog.log("${Constants.STATS_REWARD_DOUBLE}_${if (cash) Constants.STATS_REWARD_CASH else Constants.STATS_REWARD_SCORE}_${Constants.STATS_SHOW}")
    }

    private fun dismissDialog(multiple: Int) {
        this.multiple = multiple
        if (activity != null && !requireActivity().isFinishing) {
            dismissAllowingStateLoss()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (ignore: IllegalStateException) {
            ignore.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callback?.invoke(requireArguments().getInt("index"), multiple)
    }

    companion object {
        fun create(index: Int, score: Int, cash: Boolean = false, closeable: Boolean = false): RewardDialog {
            val rewardDialog = RewardDialog()
            val bundle = Bundle()
            bundle.putInt("index", index)
            bundle.putInt("score", score)
            bundle.putBoolean("cash", cash)
            bundle.putBoolean("closeable", closeable)
            rewardDialog.arguments = bundle
            return rewardDialog
        }
    }
}