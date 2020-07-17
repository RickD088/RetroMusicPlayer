package code.name.monkey.retromusic.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.android.synthetic.main.dialog_confirm.*

class ConfirmDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_confirm, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        RewardManager.rewardData?.run {
            tv_content.text = String.format(getString(R.string.cashout_warning), cashOutThreshold / 100f - cash / 100f)
        }
        btn_confirm.setOnClickListener {
            dismissDialog()
        }
        globe.setOnClickListener {
            dismissDialog()
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (ignore: IllegalStateException) {
            ignore.printStackTrace()
        }
    }

    private fun dismissDialog() {
        if (activity != null && !requireActivity().isFinishing) {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG = "ConfirmDialog"
        fun create(): ConfirmDialog {
            return ConfirmDialog()
        }
    }
}