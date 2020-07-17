package code.name.monkey.retromusic.service

import android.util.Log
import code.name.monkey.retromusic.abram.EventLog
import code.name.monkey.retromusic.abram.RemoteConfig
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService


class PushService : FirebaseMessagingService() {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed push token: $token")
        EventLog.log("push_token_x_retrieved_new")
        // Get new Instance ID token
        RemoteConfig.setPushToken(token)
    }

    companion object {
        private val TAG = javaClass.simpleName
        fun init() {
            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Push token failed: ${task.exception}")
                        EventLog.log("push_token_x_retrieve_fail")
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result?.token ?: ""
                    Log.i(TAG, "Push token success: $token" )
                    EventLog.log("push_token_x_retrieved")
                    RemoteConfig.setPushToken(token)
                })
        }
    }
}
