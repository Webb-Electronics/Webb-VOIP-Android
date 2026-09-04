package ca.webb.mobile.companionapp.voip.android.ui.sip.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.SipNotificationManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.MessageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.view.MessagePage
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbVOIPTheme

class MessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LinphoneConnector.createAndBindService(this)
        enableEdgeToEdge()
        setContent {
            WebbVOIPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Activity Parameters
                    val localUser = intent.getStringExtra("localUser") ?: "Unknown"
                    val remoteUser = intent.getStringExtra("remoteUser") ?: "Unknown"
                    // Cancel Notification to avoid stacking 25+ notifications
                    SipNotificationManager.cancelNotification(this, (localUser + remoteUser).toInt())
                    PersistentMessage.updateMessageReadStatus(
                        LocalContext.current,
                        localUser,
                        remoteUser,
                        true
                    )
                    MessagePage(localUser, remoteUser, MessageVM(this, localUser, remoteUser))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LinphoneConnector.unbindService(this)
        } catch (_: Exception) {

        }
    }

}
