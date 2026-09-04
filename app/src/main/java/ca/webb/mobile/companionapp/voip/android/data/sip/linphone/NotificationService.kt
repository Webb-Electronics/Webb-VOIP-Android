package ca.webb.mobile.companionapp.voip.android.data.sip.linphone

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Notification service
 *
 * @constructor Create a notification service
 */
internal class NotificationService : Service() {

    private val binder = NotificationBinder()

    private var linphoneEngineCore: LinphoneEngineCore? = null
    override fun onCreate() {
        super.onCreate()
        SipNotificationManager.createNotificationChannel(this)

        val notification = NotificationCompat.Builder(this, OTHER_NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Webb VOIP").setContentText("Webb VOIP is running in the background")
            .setPriority(NotificationCompat.PRIORITY_LOW).build()
        linphoneEngineCore = LinphoneEngineCore(this)
        linphoneEngineCore?.getDefaultAccount()
        startForeground(16561212, notification)

    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up audio focus and other resources
        linphoneEngineCore?.cleanup()
        linphoneEngineCore = null
    }

    internal inner class NotificationBinder : Binder() {
        fun getLinphoneEngineCore(): LinphoneEngineCore? {
            return linphoneEngineCore
        }
    }
}