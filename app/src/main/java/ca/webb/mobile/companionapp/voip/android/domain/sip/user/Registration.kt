package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import android.content.Intent
import android.util.Log
import ca.webb.mobile.companionapp.voip.android.data.roomdata.WebbRoomDBManager
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.CallAction
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneAccount
import ca.webb.mobile.companionapp.voip.android.domain.InterfaceObserved
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.MessageSenderData
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus.REGISTERED
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus.UNREGISTERED
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol.UDP
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.CallActivity
import com.google.gson.Gson

/**
 * Registration status
 *
 * @property REGISTERED registered
 * @property UNREGISTERED unregistered
 * @property REGISTERING registering
 */
enum class RegistrationStatus {
    REGISTERED, UNREGISTERED, REGISTERING,
}

/**
 * SIP transport protocol
 *
 * @property UDP udp
 * @property TLS tls
 */
enum class SIPTransportProtocol {
    UDP, TLS
}

/**
 * Registration data
 *
 * @property url the url
 * @property username the username
 * @property password the password
 * @property transport the transport
 * @constructor Create registration data with registration object
 */
data class RegistrationData(
    var url: String = "",
    var username: String = "",
    var password: String = "",
    var transport: SIPTransportProtocol = UDP
) {
    fun equivalent(registration: Registration): Boolean {
        return registration.username == username && registration.password == password && registration.url == url && registration.protocol == transport
    }

    fun looseEquivalent(registration: Registration): Boolean {
        return registration.username == username && registration.url == url
    }

    fun dataValid(): Boolean {
        return this.url.isNotBlank() && this.username.isNotBlank() && this.url.contains(" ")
            .not() && this.username.contains(" ").not() && this.password.contains(" ").not()
    }

    constructor(registration: Registration) : this(
        registration.url, registration.username, registration.password, registration.protocol
    )

}

/**
 * Registration
 *
 * @property username the username
 * @property password the password
 * @property url the url
 * @property registrationStatus the registration status
 * @property protocol the protocol
 * @property sipAccount the sip account
 * @property realName the name based on the contact manager
 * @constructor Create registration with registration data or manual input
 */
class Registration {


    lateinit var username: String
        private set
    lateinit var password: String
        private set
    lateinit var url: String
        private set
    val registrationStatus = InterfaceObserved(UNREGISTERED)

    lateinit var protocol: SIPTransportProtocol
        private set

    var sipAccount: LinphoneAccount? = null
        private set

    val realName: String?
        get() = ContactManager.findContactName(username, this)

    /**
     * Register current registration to the server
     */
    fun register() {
        if (registrationStatus.get() != UNREGISTERED) {
            throw IllegalStateException("Cannot register when already registered")
        }
        this.url = url.lowercase()
        createSIPAccountWithoutRegistration()
        sipAccount?.register()
    }

    /**
     * Unregister current registration from the server
     */
    fun unregister() {
        sipAccount?.unregister()
        sipAccount = null
    }

    /**
     * Set as default registration
     */
    fun setAsDefault() {
        if (registrationStatus.get() != REGISTERED) {
            throw IllegalStateException("Cannot set as default when not registered")
        }
        sipAccount?.setAsDefault()
    }

    /**
     * Dial the number
     *
     * @param context the context
     * @param number the number
     */
    fun dial(context: Context, number: String) {
        if (registrationStatus.get() != REGISTERED) {
            throw IllegalStateException("Cannot dial when not registered")
        }
        sipAccount?.dial(
            User.getAutoCameraOff(context),
            User.getOnlyAudioCall(context),
            number,
            ContactManager.findContactName(number, this)
        )
        // new intent of call activity
        val intent = Intent(context, CallActivity::class.java)
        intent.action = CallAction.OUTGOING.name
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    /**
     * Create Room
     */
    fun createRoom(context:Context,  remoteUser: String ) {
        if (registrationStatus.get() != REGISTERED) {
            throw IllegalStateException("Cannot send message when not registered")
        }
        WebbRoomDBManager.createChatRoom(context, username, remoteUser)
        sipAccount?.createChatRoom(remoteUser)
    }

    /**
     * Delete Room
     */

    /**
     * Send Message
     */
    fun sendMessage(context:Context, localUser: String, remoteUser: String, message:String, messages: List<MessageSenderData>) {
        if (registrationStatus.get() != REGISTERED) {
            throw IllegalStateException("Cannot send message when not registered")
        }
        val deserializeMessage = Gson().toJson(messages)
        WebbRoomDBManager.createChatRoom(context, localUser, remoteUser, deserializeMessage)
        sipAccount?.sendMessage(
            remoteUser,
            message
        )
        // need ui update for read status
        PersistentMessage.requireUpdate()
    }

    /**
     * Create SIP account without registration in Linphone
     */
    fun createSIPAccountWithoutRegistration() {
        if (sipAccount != null) {
            sipAccount?.unregister()
        }
        sipAccount = LinphoneAccount(this) {
            registrationStatus.set(it)
            Log.i("Registration", "Status updated to $it")
        }
    }

    /**
     * Update with new data
     *
     * @param context the context
     * @param registrationData the registration data
     */
    fun updateWithNewData(context: Context, registrationData: RegistrationData) {
        if (registrationStatus.get() != UNREGISTERED) {
            throw IllegalStateException("Cannot update when registered")
        }
        if (registrationData.url.isBlank() || registrationData.username.isBlank()) {
            throw IllegalArgumentException("Cannot update with empty url or username")
        }
        WebbRoomDBManager.saveRegistrationData(context, RegistrationData(this), registrationData)
        url = registrationData.url
        username = registrationData.username
        password = registrationData.password
        protocol = registrationData.transport
    }

    constructor(
        username: String?, password: String?, domain: String?, protocol: SIPTransportProtocol?
    ) {
        if (username != null) {
            this.username = username
        }
        if (password != null) {
            this.password = password
        }
        if (domain != null) {
            this.url = domain
        }
        if (protocol != null) {
            this.protocol = protocol
        }
    }

    constructor(registrationData: RegistrationData?) : this(
        registrationData?.username,
        registrationData?.password,
        registrationData?.url,
        registrationData?.transport
    )

}