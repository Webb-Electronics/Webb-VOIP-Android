package ca.webb.mobile.companionapp.voip.android.data.sip.linphone

import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol
import org.linphone.core.tools.Log

/**
 * Linphone account
 *
 * @constructor Create an linphone account
 *
 * @param registration the registration
 * @param updateStatusFunc the update status function
 */
class LinphoneAccount(
    registration: Registration, private val updateStatusFunc: (RegistrationStatus) -> Unit
) {
    val username: String = registration.username
    val password: String = registration.password
    val domain: String = registration.url
    var status: RegistrationStatus = registration.registrationStatus.get()
        internal set(status) {
            field = status
            updateStatusFunc(status)
            Log.i("LinphoneAccount", "Status updated to $status")
        }

    val protocol: SIPTransportProtocol = registration.protocol

    /**
     * Register the account with the server
     *
     */
    fun register() {
        if (status == RegistrationStatus.REGISTERED) {
            throw IllegalStateException("Cannot register when already registered")
        }
        LinphoneConnector.engineCoreInstance?.addAccount(this)
    }

    /**
     * Unregister the account with the server
     *
     */
    fun unregister() {
        LinphoneConnector.engineCoreInstance?.removeAccount(this)
    }

    /**
     * Set as default account
     *
     */
    fun setAsDefault() {
        LinphoneConnector.engineCoreInstance?.setDefaultAccount(this)
    }

    /**
     * Dial a number
     *
     * @param cameraOff the camera off
     * @param audioOnly the audio only
     * @param number the number
     * @param displayName the display name
     */
    fun dial(cameraOff: Boolean, audioOnly: Boolean, number: String, displayName: String? = null) {
        LinphoneConnector.engineCoreInstance?.dial(this, cameraOff, audioOnly, number, displayName)
    }

    /**
     * Create Chat Room
     *
     * @param remoteUser the chat recipient name
     */
    fun createChatRoom(remoteUser: String) {
        LinphoneConnector.engineCoreInstance?.createChatRoom(remoteUser, this)
            ?: throw IllegalStateException("Could not create chat room")
    }

    /**
     * Send a Message
     *
     * @param receiver the chat recipient name
     * @param message the text message to send
     */
    fun sendMessage(receiver: String, message: String) {
        LinphoneConnector.engineCoreInstance?.sendMessage(
            receiver = receiver,
            sendMessage = message,
            this
        )
    }

}