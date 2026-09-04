package ca.webb.mobile.companionapp.voip.android.data.sip.linphone

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat

/**
 * Linphone connector
 *
 * @constructor SINGLETON OBJECT
 */
object LinphoneConnector {
    /**
     * Linphone engine core instance
     *
     */
    var engineCoreInstance: LinphoneEngineCore? = null
        private set

    private val connection = LinphoneServiceConnection()

    /**
     * Create and bind service to activity
     *
     * @param activity the activity to bind the service to
     */
    fun createAndBindService(activity: ComponentActivity) {
        SipNotificationManager.requestPermission(activity) {
            val intent = Intent(activity, NotificationService::class.java)
            ActivityCompat.startForegroundService(activity, intent)
            activity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    /**
     * Unbind service from activity
     *
     * @param activity the activity to unbind the service from
     */
    fun unbindService(activity: ComponentActivity) {
        activity.unbindService(connection)
    }

    private class LinphoneServiceConnection : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NotificationService.NotificationBinder
            engineCoreInstance = binder.getLinphoneEngineCore()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            engineCoreInstance = null
        }

    }
}