package ca.webb.mobile.companionapp.voip.android.ui

import android.content.ComponentName
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ca.webb.mobile.companionapp.voip.android.BuildConfig
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.SipNotificationManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.ConnectionService
import ca.webb.mobile.companionapp.voip.android.ui.sip.container.view.SipContainerPage
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbVOIPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register Voip to System Level
        val telecomManager = getSystemService(TelecomManager::class.java)
        val componentName = ComponentName(this, ConnectionService::class.java)
        val accountId = BuildConfig.UNIQUE_VOIP_ID
        val phoneAccountHandle = PhoneAccountHandle(componentName, accountId)
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "Webb Voip")
            .setCapabilities(
                //CAPABILITY_CALL_PROVIDER
                PhoneAccount.CAPABILITY_SELF_MANAGED)
            .build()
        telecomManager.registerPhoneAccount(phoneAccount)

        setContent {
            WebbVOIPTheme {
                // A surface container using the 'background' color from the theme
                SipContainerPage()
            }
        }
        User.fillMissingRegistrationFromDB(this)
        LinphoneConnector.createAndBindService(this)
        ContactManager.loadFromRoomData(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        LinphoneConnector.unbindService(this)
    }

    override fun onResume() {
        super.onResume()
        // Resend incoming call notifications when the app is resumed
        // This ensures incoming call notifications are visible when user switches back to the app
        if (SipNotificationManager.hasIncomingCalls()) {
            SipNotificationManager.resendIncomingNotifications(this)
        }
    }

}
