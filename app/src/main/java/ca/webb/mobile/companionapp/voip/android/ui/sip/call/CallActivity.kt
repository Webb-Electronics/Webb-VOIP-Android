package ca.webb.mobile.companionapp.voip.android.ui.sip.call

import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallManager
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.view.CallContainerPage
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbVOIPTheme

class CallActivity : ComponentActivity() {
    private lateinit var powerManager: PowerManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var recentCamera: String

    override fun onCreate(savedInstanceState: Bundle?) {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        })

        super.onCreate(savedInstanceState)
        LinphoneConnector.createAndBindService(this)
        val action = CallAction.valueOf(intent.action.toString())
        handleIntent(intent)
        when (action) {
            CallAction.ACCEPT, CallAction.OUTGOING -> {
                if (CallManager.carrierCallExists) {
                    CallManager.currentConnection?.onAnswer()
                }
                enableEdgeToEdge()
                setContent {
                    WebbVOIPTheme {
                        // A surface container using the 'background' color from the theme
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            CallContainerPage {
                                finish()
                            }
                        }
                    }
                }
            }

            else -> {
                finish()
            }

        }
        powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "WebbVoip:WakeLogTag"
        )
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    override fun onRestart() {
        super.onRestart()
        PersistentCallStatus.cameraSource = recentCamera
    }

    override fun onStop() {
        super.onStop()
        recentCamera = PersistentCallStatus.cameraSource.toString()
        PersistentCallStatus.cameraSource = "StaticImage: Static picture"
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.currentConnection?.onDisconnect()
        val filteredCalls =
            PersistentCallStatus.allCalls.filter { it.value.callState != CallState.INCOMING }
        for (call in filteredCalls) {
            PersistentCallStatus.terminateCall(call.key)
        }
        try {
            LinphoneConnector.unbindService(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val action = CallAction.valueOf(intent.action.toString())
        if (intent.hasExtra("CallId")) {
            val callId = intent.getIntExtra("CallId", 0)

            when (action) {
                CallAction.ACCEPT -> {
                    LinphoneConnector.engineCoreInstance?.unjoinAllCalls()
                    PersistentCallStatus.pauseAllOngoingCalls()
                    LinphoneConnector.engineCoreInstance?.acceptCall(callId)
                }

                CallAction.REJECT -> {
                    LinphoneConnector.engineCoreInstance?.rejectCall(callId)
                }

                CallAction.END -> {
                    LinphoneConnector.engineCoreInstance?.endCall(callId)
                }

                else -> {
                    // DO NOTHING
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure wakeLock is acquired when returning from another call or activity
        if (!wakeLock.isHeld) {
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/)
        }
    }

    override fun onPause() {
        super.onPause()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}