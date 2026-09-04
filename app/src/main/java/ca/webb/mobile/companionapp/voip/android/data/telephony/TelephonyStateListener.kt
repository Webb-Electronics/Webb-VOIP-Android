package ca.webb.mobile.companionapp.voip.android.data.telephony

import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallManager

/**
 * Telephony state listener to monitor carrier call states
 * and automatically pause/resume VoIP calls accordingly
 */
abstract class TelephonyStateListener(protected val context: Context) {
    
    protected var wasCallPausedByCarrier = false
    protected val pausedCallsByCarrier = mutableSetOf<Int>()
    
    companion object {
        private const val TAG = "TelephonyStateListener"
        
        /**
         * Creates the appropriate telephony state listener based on the device's API level
         */
        fun create(context: Context): TelephonyStateListener {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ModernTelephonyStateListener(context)
            } else {
                LegacyTelephonyStateListener(context)
            }
        }
    }

    /**
     * Register this listener with the telephony service
     */
    abstract fun register()
    
    /**
     * Unregister this listener from the telephony service
     */
    abstract fun unregister()
    
    /**
     * Handle carrier call state changes
     */
    protected fun handleCallStateChanged(state: Int, phoneNumber: String? = null) {
        Log.d(TAG, "Carrier call state changed: $state, phoneNumber: $phoneNumber")
        
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                Log.d(TAG, "Carrier call ringing - preparing for potential call")
            }
            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (!CallManager.carrierCallExists) {
                    handleCarrierCallActive()
                }
            }
            TelephonyManager.CALL_STATE_IDLE -> {
                handleCarrierCallEnded()
            }
        }
    }
    
    protected fun handleCarrierCallActive() {
        Log.d(TAG, "Carrier call is active - pausing all ongoing VoIP calls")

        CallManager.carrierCallExists = true
        pausedCallsByCarrier.clear()

        val activeCalls = PersistentCallStatus.getActiveConnectedCalls()

        if (activeCalls.isNotEmpty()) {
            activeCalls.forEach { uuid ->
                Log.d(TAG, "Pausing VoIP call: $uuid")
                PersistentCallStatus.pauseCall(uuid, true)
                pausedCallsByCarrier.add(uuid)
            }
            wasCallPausedByCarrier = true
        }
    }
    
    protected fun handleCarrierCallEnded() {
        Log.d(TAG, "Carrier call ended - resuming previously paused VoIP calls")

        CallManager.carrierCallExists = false

        if (wasCallPausedByCarrier && pausedCallsByCarrier.isNotEmpty()) {
            Log.d(TAG, "Resuming VoIP calls: ${pausedCallsByCarrier.joinToString()}")
            PersistentCallStatus.resumeCallsByUuid(pausedCallsByCarrier.toList())
            
            pausedCallsByCarrier.clear()
            wasCallPausedByCarrier = false
        }
    }
}

/**
 * Modern implementation using TelephonyCallback (Android 12/API 31+)
 */
@RequiresApi(Build.VERSION_CODES.S)
class ModernTelephonyStateListener(context: Context) : TelephonyStateListener(context) {
    
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    private val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            handleCallStateChanged(state)
        }
    }
    
    override fun register() {
        telephonyManager.registerTelephonyCallback(context.mainExecutor, telephonyCallback)
    }
    
    override fun unregister() {
        telephonyManager.unregisterTelephonyCallback(telephonyCallback)
    }
}

/**
 * Legacy implementation using PhoneStateListener for backward compatibility
 */
@Suppress("DEPRECATION")
class LegacyTelephonyStateListener(context: Context) : TelephonyStateListener(context) {

    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            handleCallStateChanged(state, phoneNumber)
        }
    }

    override fun register() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun unregister() {
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
    }
}
