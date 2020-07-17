package code.name.monkey.retromusic.dialogs

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.android.synthetic.main.fragment_reward_cash.*

class RewardCashBottomSheetDialogFragment : DialogFragment() {

    var cashoutCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reward_cash, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        seek_bar.isEnabled = false
        ObjectAnimator.ofFloat(container, "translationY", 200f, 0f).start()
        RewardManager.rewardData?.run {
            tv_cash.text = String.format(getString(R.string.cash_prefix), cash / 100f)
            seek_bar.max = cashOutThreshold
            seek_bar.progress = cash
            tv_cashout_label.text = String.format(getString(R.string.cashout_threshold), cashOutThreshold / 100)
        }
        setOnClickListener()
    }

    private fun dismissDialog() {
        if (activity != null && !requireActivity().isFinishing) {
            dismissAllowingStateLoss()
        }
    }

    private fun setOnClickListener() {
        btn_close.setOnClickListener {
            dismissDialog()
        }
        globe.setOnClickListener {
            dismissDialog()
        }
        btn_cashout.setOnClickListener {
            RewardManager.rewardData?.run {
                if (cash < cashOutThreshold) {
                    ConfirmDialog.create().show(parentFragmentManager, ConfirmDialog.TAG)
                } else {
                    val dialog = CashoutDialog.create()
                    dialog.cashoutCallback = {
                        cashoutCallback?.invoke(it)
                        dismissDialog()
                    }
                    dialog.show(parentFragmentManager, CashoutDialog.TAG)
                }
            }
        }
    }

    companion object {
        const val TAG = "RewardCashBottomSheetDialogFragment"

        fun create(): RewardCashBottomSheetDialogFragment {
            return RewardCashBottomSheetDialogFragment()
        }
    }
}