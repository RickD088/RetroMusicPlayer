package code.name.monkey.retromusic.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.util.RewardManager
import kotlinx.android.synthetic.main.dialog_cashout.*

class CashoutDialog : DialogFragment() {

    var cashoutCallback: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogFullScreen)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_cashout, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_confirm.setOnClickListener {
            if (editText.text.trim().isEmpty()) {
                Toast.makeText(context, getString(R.string.input_account_please), Toast.LENGTH_SHORT).show()
            } else {
                cashoutCallback?.invoke(editText.text.trim().toString())
                dismissDialog()
            }
        }
        globe.setOnClickListener {
            dismissDialog()
        }
    }

    private fun dismissDialog() {
        if (activity != null && !requireActivity().isFinishing) {
            dismissAllowingStateLoss()
        }
    }

    companion object {
        const val TAG = "CashoutDialog"
        fun create(): CashoutDialog {
            return CashoutDialog()
        }
    }
}