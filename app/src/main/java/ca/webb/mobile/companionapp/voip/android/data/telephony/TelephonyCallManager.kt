package ca.webb.mobile.companionapp.voip.android.data.telephony

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.content.ContextCompat

/**
 * Manager for telephony state monitoring to handle carrier call interference
 * with VoIP calls
 */
class TelephonyCallManager(private val context: Context) {
    
    companion object {
        private const val TAG = "TelephonyCallManager"
    }
    
    private var telephonyManager: TelephonyManager? = null
    private var telephonyStateListener: TelephonyStateListener? = null
    private var isListening = false
    
    /**
     * Start monitoring carrier call states
     */
    fun startMonitoring() {
        if (isListening) {
            Log.d(TAG, "Already monitoring telephony state")
            return
        }
        
        // Check if we have the required permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "READ_PHONE_STATE permission not granted - cannot monitor carrier calls")
            return
        }
        
        try {
            telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            // Use the factory method to create the appropriate listener based on API level
            telephonyStateListener = TelephonyStateListener.create(context)
            
            // Register the listener
            telephonyStateListener?.register()
            isListening = true
            
            Log.d(TAG, "Started monitoring carrier call states")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start telephony monitoring", e)
        }
    }
    
    /**
     * Stop monitoring carrier call states
     */
    fun stopMonitoring() {
        if (!isListening) {
            return
        }
        
        try {
            telephonyStateListener?.unregister()
            
            telephonyStateListener = null
            telephonyManager = null
            isListening = false
            
            Log.d(TAG, "Stopped monitoring carrier call states")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop telephony monitoring", e)
        }
    }

}
