package ca.webb.mobile.companionapp.voip.android.data.sip.linphone


import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import ca.webb.mobile.companionapp.voip.android.BuildConfig
import ca.webb.mobile.companionapp.voip.android.R
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction.ACCEPT
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction.END
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction.OTHER
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction.OUTGOING
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction.REJECT
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.NotificationState.ENDED
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.NotificationState.INCOMING
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.NotificationState.ONGOING
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.NotificationState.OUTGOING
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallManager
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.ConnectionService
import ca.webb.mobile.companionapp.voip.android.ui.MainActivity
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.CallActivity
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.MessageActivity
import org.linphone.core.Call
import kotlin.random.Random


internal const val INCOMING_NOTIFICATION_CHANNEL_ID = "Webb VOIP incoming calls"
internal const val INCOMING_NOTIFICATION_CHANNEL_NAME = "Webb VOIP for incoming calls"
internal const val INCOMING_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Webb VOIP Notifications for incoming calls"

internal const val OTHER_NOTIFICATION_CHANNEL_ID = "Webb VOIP other notifications"
internal const val OTHER_NOTIFICATION_CHANNEL_NAME = "Webb VOIP for other notifications"
internal const val OTHER_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Webb VOIP Notifications for other notifications"

/**
 * Notification states
 *
 * @property INCOMING Incoming call
 * @property OUTGOING Outgoing call
 * @property ENDED Call ended
 * @property ONGOING Call ongoing
 */

enum class NotificationState {
    INCOMING, OUTGOING, ENDED, ONGOING;

    companion object {
        /**
         * Translate call state to notification state
         *
         * @param callState the call state
         * @return the notification state
         */
        fun translateFromCallState(callState: Call.State): NotificationState {
            return when (callState) {
                Call.State.OutgoingInit, Call.State.OutgoingProgress, Call.State.OutgoingRinging -> OUTGOING
                Call.State.IncomingReceived, Call.State.IncomingEarlyMedia, Call.State.PushIncomingReceived -> INCOMING
                Call.State.End, Call.State.Idle, Call.State.Error, Call.State.Released -> ENDED
                else -> ONGOING
            }
        }
    }

}

/**
 * Call actions
 *
 * @property ACCEPT Accept the call
 * @property REJECT Reject the call
 * @property END End the call
 * @property OUTGOING Make an outgoing call
 * @property OTHER other
 */
enum class CallAction {
    ACCEPT, REJECT, END, OUTGOING, OTHER
}

/**
 * Sip notification manager
 *
 * @constructor Singleton object
 */
internal object SipNotificationManager {
    private var isOnCall: Boolean = false
    val getOnCallStatus: Boolean
        get() = isOnCall

    private data class NotificationBundle(
        val call: Call, val id: Int, val currentState: NotificationState
    )

    private var allCalls = mutableListOf<NotificationBundle>()

    /**
     * Get call by id
     *
     * @param id the id of the call
     * @return the call
     */
    fun getCallById(id: Int): Call? {
        return allCalls.firstOrNull { it.id == id }?.call
    }

    /**
     * Handle call status change
     *
     * @param context the context
     * @param state the state of the call
     * @param call the call
     * @return the id of the call
     */
    fun handleCallStatusChange(context: Context, state: Call.State, call: Call): Int? {
        var existingObject: NotificationBundle? = null
        val notificationState = NotificationState.translateFromCallState(state)
        allCalls.firstOrNull { it.call == call }?.let {
            if (it.currentState != notificationState) {
                cancelNotification(context, it.id)
            }
            existingObject = it
        }
        if (existingObject?.currentState == notificationState) {
            return existingObject.id
        }
        val name = call.remoteAddress.displayName ?: call.remoteAddress.username ?: "Unknown Dialer"
        when (notificationState) {
            NotificationState.INCOMING -> {
                val id = createIncomingNotification(context, name, existingObject?.id)
                allCalls.add(NotificationBundle(call, id, notificationState))
                return id
            }

            NotificationState.OUTGOING -> {
                val id = createOngoingNotification(context, name, existingObject?.id)
                allCalls.add(NotificationBundle(call, id, notificationState))
                return id
            }

            NotificationState.ENDED -> {
                isOnCall = false
                existingObject?.let {
                    allCalls.remove(it)
                }
                return existingObject?.id
            }

            NotificationState.ONGOING -> {
                isOnCall = true
                val id = allCalls.firstOrNull { it.call == call }?.id
                if (id != null) {
                    createOngoingNotification(context, name, id)
                }
                return id
            }
        }
    }


    /**
     * Request permissions for the app
     *
     * @param activity the activity
     * @param onGranted the function to run when permission is granted
     */
    fun requestPermission(activity: ComponentActivity, onGranted: () -> Unit) {
        createNotificationChannel(activity)
        // Android 12-
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )
        // Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        //Android 14+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }

        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.all { it.value }) {
                onGranted()
                Log.i("SipNotificationManager", "Permission granted")
            } else {
                Log.e("SipNotificationManager", "Permission not granted")
            }
        }.launch(permissionsToRequest.toTypedArray())
    }

    /**
     * Create notification channel for the app
     *
     * @param context the context of the app
     */
    fun createNotificationChannel(context: Context) {
        val importance = NotificationManager.IMPORTANCE_HIGH
        val incomingChannel = NotificationChannel(
            INCOMING_NOTIFICATION_CHANNEL_ID, INCOMING_NOTIFICATION_CHANNEL_NAME, importance
        ).apply {
            description = INCOMING_NOTIFICATION_CHANNEL_DESCRIPTION
        }
        incomingChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
        incomingChannel.enableVibration(true)

        val otherChannel = NotificationChannel(
            OTHER_NOTIFICATION_CHANNEL_ID, OTHER_NOTIFICATION_CHANNEL_NAME, importance
        ).apply {
            description = OTHER_NOTIFICATION_CHANNEL_DESCRIPTION
        }
        otherChannel.enableVibration(false)

        val notificationManager: NotificationManager =
            context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(incomingChannel)
        notificationManager.createNotificationChannel(otherChannel)
    }

    fun isPhoneAccountEnabled(context: Context): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val handle = PhoneAccountHandle(
            ComponentName(context, ConnectionService::class.java),
            BuildConfig.UNIQUE_VOIP_ID
        )
        val account = telecomManager.getPhoneAccount(handle)
        return account?.isEnabled == true
    }

    private fun createIncomingNotification(
        context: Context, name: String, existingId: Int? = null
    ): Int {

        // Incomming Voip Call as a system level if there is ongoing carrier call
        if (CallManager.carrierCallExists){
            addIncommingVoipCallAsSystemCall(context)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return createLegacyCallNotification(
                context, "Incoming Call", "$name is calling you", existingId
            )
        }
        val id = existingId ?: getRandomPositiveInt()
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )
        val rejecting = Intent(context, CallActivity::class.java)
        rejecting.putExtra("CallId", id)
        rejecting.action = CallAction.REJECT.name

        val rejectingPendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), rejecting, PendingIntent.FLAG_IMMUTABLE
        )

        val accepting = Intent(context, CallActivity::class.java)
        accepting.putExtra("CallId", id)
        accepting.action = CallAction.ACCEPT.name
        val acceptingPendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), accepting, PendingIntent.FLAG_IMMUTABLE
        )
        val person = Person.Builder()
            .setIcon(IconCompat.createWithResource(context, R.drawable.round_person_24))
            .setName(name).setImportant(true).build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, INCOMING_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_local_phone_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(false).setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL).setOngoing(true)
            .setFullScreenIntent(pendingIntent, true).setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    person,
                    rejectingPendingIntent,
                    acceptingPendingIntent,

                    )
            ).addPerson(person).build()

        notificationManager.notify(id, notification)
        return id
    }

    fun cancelNotification(context: Context, id: Int) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

    private fun createOngoingNotification(
        context: Context, name: String, existingId: Int? = null
    ): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return createLegacyCallNotification(
                context, "Ongoing Call", "$name is calling you", existingId, ongoing = true
            )
        }
        val id = existingId ?: getRandomPositiveInt()
        val intent = Intent(context, CallActivity::class.java)
        intent.action = CallAction.OTHER.name
        val pendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )

        val eIntent = Intent(context, CallActivity::class.java)
        eIntent.putExtra("CallId", id)
        eIntent.setAction(CallAction.END.name)
        val ePendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), eIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val person = Person.Builder()
            .setIcon(IconCompat.createWithResource(context, R.drawable.round_person_24))
            .setName(name).setImportant(true).build()

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, OTHER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_local_phone_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setOngoing(true)
            .setFullScreenIntent(pendingIntent, true).setStyle(
                NotificationCompat.CallStyle.forOngoingCall(person, ePendingIntent)
            ).addPerson(person).build()
        notificationManager.notify(id, notification)
        return id
    }

    private fun createLegacyCallNotification(
        context: Context, title: String, message: String, existingId: Int?, ongoing: Boolean = false
    ): Int {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val id = existingId ?: getRandomPositiveInt()
        val notification = NotificationCompat.Builder(
            context,
            if (ongoing) OTHER_NOTIFICATION_CHANNEL_ID else INCOMING_NOTIFICATION_CHANNEL_ID
        ).setContentTitle(title).setContentText(message)
            .setSmallIcon(R.drawable.baseline_local_phone_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setAutoCancel(false)
        if (ongoing) {
            val intent = Intent(context, CallActivity::class.java)
            intent.putExtra("CallId", id)
            intent.setAction(CallAction.END.name)
            val pendingIntent = PendingIntent.getActivity(
                context, getRandomPositiveInt(), intent, PendingIntent.FLAG_IMMUTABLE
            )
            notification.addAction(R.drawable.baseline_check_circle_24, "End", pendingIntent)

        } else {
            val declinedIntent = Intent(context, CallActivity::class.java)
            declinedIntent.putExtra("CallId", id)
            declinedIntent.setAction(CallAction.REJECT.name)
            val declinedPendingIntent = PendingIntent.getActivity(
                context, getRandomPositiveInt(), declinedIntent, PendingIntent.FLAG_IMMUTABLE
            )
            val acceptedIntent = Intent(context, CallActivity::class.java)
            acceptedIntent.putExtra("CallId", id)
            acceptedIntent.setAction(CallAction.ACCEPT.name)
            val acceptedPendingIntent = PendingIntent.getActivity(
                context, getRandomPositiveInt(), acceptedIntent, PendingIntent.FLAG_IMMUTABLE
            )
            notification.addAction(
                R.drawable.baseline_check_circle_24, "Accept", acceptedPendingIntent
            ).addAction(R.drawable.baseline_cancel_24, "Decline", declinedPendingIntent)

        }
        notificationManager.notify(id, notification.build())
        return id
    }

    private fun getRandomPositiveInt(): Int {
        return Random.nextInt(0, Int.MAX_VALUE)
    }

    private fun addIncommingVoipCallAsSystemCall(context:Context) {
        try {
            val telecomManager =
                context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val handle = PhoneAccountHandle(
                ComponentName(context, ConnectionService::class.java),
                BuildConfig.UNIQUE_VOIP_ID
            )
            val extras = Bundle().apply {
                putParcelable(
                    TelecomManager.EXTRA_INCOMING_CALL_ADDRESS,
                    Uri.fromParts("sip", BuildConfig.UNIQUE_CALLER_ID, null)
                )
            }
            telecomManager.addNewIncomingCall(handle, extras)
        } catch (e: Exception) {
            println(e)
        }
    }

    fun createIncomingMessageNotification(
        context: Context, localUser: String, remoteUser: String, message: String?
    ) {
        val intentMsg = Intent(context, MessageActivity::class.java)
        intentMsg.putExtra("localUser", localUser)
        intentMsg.putExtra("remoteUser", remoteUser)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), intentMsg, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(
            context, OTHER_NOTIFICATION_CHANNEL_ID
        ).setContentTitle(
            "$remoteUser sent "
        ).setContentText(message ?: "unknown message").setSmallIcon(R.drawable.baseline_storage_24)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setAutoCancel(false)
            .setContentIntent(pendingIntent)
        notificationManager.notify((localUser + remoteUser).toInt(), notification.build())
    }

    /**
     * Create hold notification when a call is put on hold
     *
     * @param context the context
     * @param name the name of the caller
     * @param callId the call ID
     */
    fun createHoldNotification(context: Context, name: String, callId: Int) {
        val intent = Intent(context, CallActivity::class.java)
        intent.action = CallAction.OTHER.name
        intent.putExtra("CallId", callId)
        val pendingIntent = PendingIntent.getActivity(
            context, getRandomPositiveInt(), intent, PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, OTHER_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_local_phone_24).setContentTitle("Call on Hold")
            .setContentText("$name is on hold. Tap to resume.")
            .setPriority(NotificationCompat.PRIORITY_LOW).setOngoing(true).setAutoCancel(false)
            .setContentIntent(pendingIntent).setCategory(NotificationCompat.CATEGORY_STATUS).build()

        // Use a specific notification ID for hold notifications (callId + 10000 to avoid conflicts)
        notificationManager.notify(callId + 10000, notification)
    }

    /**
     * Cancel hold notification when a call is resumed
     *
     * @param context the context
     * @param callId the call ID
     */
    fun cancelHoldNotification(context: Context, callId: Int) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Cancel hold notification using the same ID formula
        notificationManager.cancel(callId + 10000)
    }

    /**
     * Resend incoming call notifications when the app is resumed
     * This helps ensure incoming call notifications are visible when user switches back to the app
     *
     * @param context the context
     */
    fun resendIncomingNotifications(context: Context) {
        allCalls.filter { it.currentState == NotificationState.INCOMING }
            .forEach { notificationBundle ->
                val call = notificationBundle.call
                val name = call.remoteAddress.displayName ?: call.remoteAddress.username
                ?: "Unknown Dialer"

                // Re-post the existing notification with the same ID to make it visible again
                createIncomingNotification(context, name, notificationBundle.id)

                Log.d(
                    "SipNotificationManager",
                    "Re-notified incoming notification for call from: $name"
                )
            }
    }

    /**
     * Check if there are any active incoming calls
     *
     * @return true if there are incoming calls, false otherwise
     */
    fun hasIncomingCalls(): Boolean {
        return allCalls.any { it.currentState == NotificationState.INCOMING }
    }
}

