package ca.webb.mobile.companionapp.voip.android.data.sip.linphone

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import ca.webb.mobile.companionapp.voip.android.data.config.DeploymentConfig
import ca.webb.mobile.companionapp.voip.android.data.telephony.TelephonyCallManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.AudioCommunicationDevice
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallInfo
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryLog
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationData
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import org.linphone.core.Account
import org.linphone.core.Address
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.CallLog
import org.linphone.core.ChatMessage
import org.linphone.core.ChatRoom
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.LogLevel
import org.linphone.core.MediaEncryption
import org.linphone.core.Reason
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Linphone engine core
 *
 * @constructor Create an linphone engine core
 *
 * @param context the context
 */
class LinphoneEngineCore(context: Context) {

    private val mCore: Core
    private var allAccounts = mutableMapOf<LinphoneAccount, Account>()
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasAudioFocus = false
    private val telephonyCallManager: TelephonyCallManager = TelephonyCallManager(context)


    init {
        val optionalConfigToInstall = Factory.instance().createConfig("linphoneCoreConfig")
        mCore = Factory.instance().createCoreWithConfig(optionalConfigToInstall, context)
        mCore.isCallkitEnabled = true
        setDefaultAudioDevice()
        mCore.addListener(CoreDelegateListener(context))
        Factory.instance().loggingService.setLogLevel(LogLevel.Error)
        mCore.start()
        mCore.playbackGainDb = 5.0f
        
        // Start monitoring carrier calls for automatic pause/resume
        telephonyCallManager.startMonitoring()
    }

    private fun setDefaultAudioDevice() {
        mCore.defaultOutputAudioDevice = mCore.audioDevices.firstOrNull {
            it.type == AudioDevice.Type.Bluetooth && (it.capabilities == AudioDevice.Capabilities.CapabilityAll || it.capabilities == AudioDevice.Capabilities.CapabilityPlay)
        }

        mCore.defaultInputAudioDevice = mCore.audioDevices.firstOrNull {
            it.type == AudioDevice.Type.Bluetooth && (it.capabilities == AudioDevice.Capabilities.CapabilityAll || it.capabilities == AudioDevice.Capabilities.CapabilityRecord)
        }
    }

    /**
     * Add account to the core
     *
     * @param acc the account to add
     */
    fun addAccount(acc: LinphoneAccount) {
        if (allAccounts.contains(acc)) {
            throw IllegalArgumentException("Account already exists")
        }
        val transport = matchSIPTransportationProtocol(acc.protocol)
        val authInfo = Factory.instance()
            .createAuthInfo(acc.username, null, acc.password, null, null, acc.domain)
        val accountParams = mCore.createAccountParams()
        val identity = Factory.instance().createAddress("sip:${acc.username}@${acc.domain}")
        identity?.password = acc.password
        identity?.domain = acc.domain
        identity?.username = acc.username
        identity?.transport = transport
        accountParams.identityAddress = identity
        val address = Factory.instance().createAddress("sip:${acc.domain}")
        address?.transport = transport
        accountParams.serverAddress = address
        accountParams.isRegisterEnabled = true
        accountParams.pushNotificationAllowed = true
        accountParams.remotePushNotificationAllowed = true
        accountParams.pushNotificationConfig.provider = "apns.dev"

        if (transport == TransportType.Tls) {
            NetworkPolicyMaker.createPolicy(mCore, acc.domain)?.let {
                accountParams.natPolicy = it
            }
        }
        val account = mCore.createAccount(accountParams)
        mCore.addAuthInfo(authInfo)
        mCore.addAccount(account)
        allAccounts[acc] = account
    }

    /**
     * Remove account
     *
     * @param acc the account to remove
     */
    fun removeAccount(acc: LinphoneAccount) {
        allAccounts[acc]?.let {
            mCore.removeAccount(it)
            allAccounts.remove(acc)
        }
        acc.status = RegistrationStatus.UNREGISTERED
        if (allAccounts.isEmpty()) {
            mCore.clearAccounts()
        }
    }

    /**
     * Set default account
     *
     * @param acc the account to set as default
     */
    fun setDefaultAccount(acc: LinphoneAccount) {
        allAccounts[acc]?.let {
            mCore.defaultAccount = it
        }
    }

    /**
     * get default account
     *
     * @return the default account or null if not found
     */
    fun getDefaultAccount(): RegistrationData? {
        mCore.defaultAccount?.let { acc ->
            val data = RegistrationData(
                acc.params.identityAddress?.username ?: "",
                acc.params.identityAddress?.password ?: "",
                acc.params.identityAddress?.domain ?: "",
                if (acc.params.identityAddress?.transport == TransportType.Tls) SIPTransportProtocol.TLS else SIPTransportProtocol.UDP
            )
            if (data.username.isEmpty() || data.password.isEmpty() || data.url.isEmpty()) {
                mCore.defaultAccount = null
                return null
            }
            return data
        }
        return null
    }

    private fun configureAudioSession() {
        mCore.configureAudioSession()
        requestAudioFocus()
    }

    /**
     * Request audio focus to pause/duck background music during calls
     */
    private fun requestAudioFocus() {
        if (hasAudioFocus) return

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            .setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setOnAudioFocusChangeListener { focusChange ->
                handleAudioFocusChange(focusChange)
            }
            .build()

        audioFocusRequest = focusRequest
        val result = audioManager.requestAudioFocus(focusRequest)
        hasAudioFocus = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    /**
     * Release audio focus when call ends
     */
    private fun releaseAudioFocus() {
        if (!hasAudioFocus) return

        audioFocusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
            audioFocusRequest = null
        }
        hasAudioFocus = false
    }

    /**
     * Handle audio focus changes during calls
     */
    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Resume normal audio processing if needed
                hasAudioFocus = true
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanently lost focus, end call if appropriate
                hasAudioFocus = false
                releaseAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                // Temporarily lost focus
                hasAudioFocus = false
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lost focus but can continue at lower volume
                // The system will handle ducking automatically
            }
        }
    }

    /**
     * Get current registrations
     *
     * @return list of current registrations
     */

    fun getCurrentRegistrations(): MutableList<Registration> {
        val regs = mutableListOf<Registration>()
        for (acc in mCore.accountList) {
            val username = acc.params.identityAddress?.username
            val url = acc.params.identityAddress?.domain
            val password = acc.params.identityAddress?.password
            if (username == null || url == null || password == null) {
                mCore.removeAccount(acc)
                continue
            }
            val transport =
                if (acc.params.identityAddress?.transport == TransportType.Tls) SIPTransportProtocol.TLS else SIPTransportProtocol.UDP
            val newReg = Registration(username, password, url, transport)
            newReg.createSIPAccountWithoutRegistration()
            if (newReg.sipAccount == null) {
                mCore.removeAccount(acc)
                continue
            }
            newReg.sipAccount?.let {
                allAccounts[it] = acc
            }
            regs.add(newReg)
        }
        return regs
    }

    private fun matchSIPTransportationProtocol(protocol: SIPTransportProtocol): TransportType {
        return when (protocol) {
            SIPTransportProtocol.UDP -> TransportType.Udp
            SIPTransportProtocol.TLS -> TransportType.Tls
        }
    }

    private inner class CoreDelegateListener(private val context: Context) : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core, account: Account, state: RegistrationState?, message: String
        ) {
            allAccounts.filter {
                account.params.identityAddress?.let { address2 ->
                    it.value.params.identityAddress?.weakEqual(
                        address2
                    )
                } == true
            }.firstNotNullOfOrNull { it.key }?.let { linphoneAccount ->
                linphoneAccount.status = when (state) {
                    RegistrationState.Ok -> RegistrationStatus.REGISTERED
                    RegistrationState.Progress, RegistrationState.Refreshing -> RegistrationStatus.REGISTERING
                    else -> RegistrationStatus.UNREGISTERED
                }
            }
            super.onAccountRegistrationStateChanged(core, account, state, message)
        }

        override fun onCallLogUpdated(core: Core, callLog: CallLog) {
            if (mCore.callLogs.size > 100 && mCore.callLogs.isNotEmpty()) {
                mCore.removeCallLog(mCore.callLogs.last())
            }
            super.onCallLogUpdated(core, callLog)
        }

        override fun onAudioDevicesListUpdated(core: Core) {
            setDefaultAudioDevice()
            super.onAudioDevicesListUpdated(core)
        }

        override fun onCallStateChanged(
            core: Core, call: Call, state: Call.State?, message: String
        ) {
            state?.let {
                SipNotificationManager.handleCallStatusChange(context, it, call)?.let { id ->
                    val displayName = PersistentCallStatus.allCalls[id]?.callingName
                        ?: call.remoteAddress.displayName ?: call.remoteAddress.username
                        ?: "Unknown"
                    val mutableAllCalls = PersistentCallStatus.allCalls.toMutableMap()
                    when (state) {
                        Call.State.OutgoingInit, Call.State.OutgoingRinging, Call.State.OutgoingProgress, Call.State.OutgoingEarlyMedia -> {
                            mutableAllCalls[id] = CallInfo(CallState.CONNECTING, displayName)
                            requestAudioFocus() // Request audio focus for outgoing calls
                        }

                        Call.State.IncomingReceived, Call.State.IncomingEarlyMedia, Call.State.PushIncomingReceived -> {
                            mutableAllCalls[id] = CallInfo(CallState.INCOMING, displayName)
                        }

                        Call.State.Released, Call.State.End, Call.State.Idle, Call.State.Error -> {
                            mutableAllCalls.remove(id)
                            // Release audio focus when all calls end
                            if (mCore.calls.isEmpty()) {
                                releaseAudioFocus()
                            }
                        }

                        Call.State.Connected, Call.State.StreamsRunning -> {
                            mutableAllCalls[id] = CallInfo(CallState.CONNECTED, displayName)
                            requestAudioFocus() // Ensure audio focus for connected calls
                        }

                        Call.State.Paused, Call.State.Pausing -> {
                            mutableAllCalls[id] = CallInfo(CallState.CONNECTED, displayName)
                            // Create hold notification when call is paused
                            SipNotificationManager.createHoldNotification(context, displayName, id)
                        }

                        Call.State.Resuming -> {
                            mutableAllCalls[id] = CallInfo(CallState.CONNECTED, displayName)
                            // Cancel hold notification when call is resumed
                            SipNotificationManager.cancelHoldNotification(context, id)
                            requestAudioFocus() // Re-request audio focus when resuming
                        }

                        Call.State.Updating, Call.State.UpdatedByRemote, Call.State.EarlyUpdating, Call.State.EarlyUpdatedByRemote -> {
                            PersistentCallStatus.requireUpdate()
                        }

                        else -> {
                            mutableAllCalls[id] = CallInfo(CallState.CONNECTED, displayName)
                        }
                    }
                    PersistentCallStatus.allCalls = mutableAllCalls.toMap()
                }
            }
            super.onCallStateChanged(core, call, state, message)
        }

        // Receive Message
        override fun onMessageReceived(core: Core, chatRoom: ChatRoom, message: ChatMessage) {
            PersistentMessage.messageReceiving(
                localUser = chatRoom.localAddress.username.toString(),
                remoteUser = chatRoom.peerAddress.username.toString(),
                message = message.utf8Text
            )

            SipNotificationManager.createIncomingMessageNotification(
                context,
                chatRoom.localAddress.username.toString(),
                chatRoom.peerAddress.username.toString(),
                message.utf8Text
            )

            super.onMessageReceived(core, chatRoom, message)
        }
    }

    /**
     * Delete Chat Room
     *
     * @param localUser the sender
     * @param remoteUser the recipient
     */
    fun deleteChatRoom(localUser: String, remoteUser: String) {
        mCore.chatRooms.filter {
            it.localAddress.username == localUser && it.peerAddress.username == remoteUser
        }.forEach { room ->
            mCore.deleteChatRoom(room)
        }
    }

    /**
     * Create Chat Room (private, helper for send message)
     *
     * @param remoteUser the recipient to create chat room with
     * @param acc the account to use
     */
    fun createChatRoom(remoteUser: String, acc: LinphoneAccount): ChatRoom? {
        val roomExist = mCore.chatRooms.filter {
            it.localAddress.username == acc.username && it.peerAddress.username == remoteUser
        }
        if (roomExist.isEmpty()) {
            val localAddress = allAccounts[acc]?.params?.identityAddress
            val remoteAddress = Factory.instance().createAddress("sip:$remoteUser@${acc.domain}")
                ?: throw IllegalArgumentException("Failed to create remote address")
            val params = mCore.createDefaultChatRoomParams()
            return mCore.createChatRoom(params, localAddress, arrayOf(remoteAddress))
        }
        return roomExist.first()
    }

    /**
     * Send Message
     *
     * @param receiver the recipient to send message
     * @param sendMessage the text message to send
     * @param acc the account to use
     */
    fun sendMessage(receiver: String, sendMessage: String, acc: LinphoneAccount) {
        val room = createChatRoom(receiver, acc)
            ?: throw IllegalStateException("Could not create chat room")
        val msg = room.createMessageFromUtf8(sendMessage)
        msg.send()
    }

    /**
     * Delete Chat Room
     *
     * @param localUser the sender
     * @param remoteUser the recipient
     */
//    fun deleteChatRoom(localUser: String, remoteUser: String) {
//        mCore.chatRooms.filter {
//            it.localAddress.username == localUser && it.peerAddress.username == remoteUser
//        }.forEach { room ->
//            mCore.deleteChatRoom(room)
//        }
//    }

    /**
     * Create Chat Room (private, helper for send message)
     *
     * @param remoteUser the recipient to create chat room with
     * @param acc the account to use
     */
//    fun createChatRoom(remoteUser: String, acc: LinphoneAccount): ChatRoom? {
//        val roomExist = mCore.chatRooms.filter {
//            it.localAddress.username == acc.username && it.peerAddress.username == remoteUser
//        }
//        if (roomExist.isEmpty()) {
//            val localAddress = allAccounts[acc]?.params?.identityAddress
//            val remoteAddress = Factory.instance().createAddress("sip:$remoteUser@${acc.domain}")
//                ?: throw IllegalArgumentException("Failed to create remote address")
//            val params = mCore.createDefaultChatRoomParams()
//            return mCore.createChatRoom(params, localAddress, arrayOf(remoteAddress))
//        }
//        return roomExist.first()
//    }

    /**
     * Send Message
     *
     * @param receiver the recipient to send message
     * @param sendMessage the text message to send
     * @param acc the account to use
     */
//    fun sendMessage(receiver: String, sendMessage: String, acc: LinphoneAccount) {
//        val room = createChatRoom(receiver, acc)
//            ?: throw IllegalStateException("Could not create chat room")
//        val msg = room.createMessageFromUtf8(sendMessage)
//        msg.send()
//    }

    /**
     * Dial a number
     *
     * @param acc the account to use
     * @param cameraOff the camera off
     * @param audioOnly the audio only
     * @param number the number
     * @param displayName the display name
     */
    fun dial(
        acc: LinphoneAccount,
        cameraOff: Boolean,
        audioOnly: Boolean,
        number: String,
        displayName: String? = null
    ) {
        if (mCore.inCall()) {
            throw IllegalStateException("Already in a call")
        }
        val params = mCore.createCallParams(null)
            ?: throw IllegalStateException("Failed to create call params")
        params.mediaEncryption = MediaEncryption.None
        params.account = allAccounts[acc]
        params.isVideoEnabled = !audioOnly
        if (cameraOff && params.isVideoEnabled) {
            PersistentCallStatus.cameraSource = "StaticImage: Static picture"
        } else {
            PersistentCallStatus.cameraSource = "FrontFacingCamera"
        }
        val remoteAddress = Factory.instance().createAddress("sip:$number@${acc.domain}")
            ?: throw IllegalArgumentException("Failed to create remote address")
        val call = mCore.inviteAddressWithParams(remoteAddress, params)
            ?: throw IllegalStateException("Failed to create call")
        call.remoteAddress.displayName = displayName
    }

    /**
     * Accept call
     *
     * @param callUUID the call uuid
     */
    fun acceptCall(callUUID: Int) {
        SipNotificationManager.getCallById(callUUID)?.let {
            configureAudioSession()
            val params = mCore.createCallParams(it)
            if (params == null) {
                it.accept()
                return
            }
            params.isVideoEnabled = it.remoteParams?.isVideoEnabled == true
            params.mediaEncryption = MediaEncryption.None
            it.acceptWithParams(params)
        }
    }

    /**
     * Reject call
     *
     * @param callUUID the call uuid
     */
    fun rejectCall(callUUID: Int) {
        SipNotificationManager.getCallById(callUUID)?.decline(Reason.Declined)
    }

    /**
     * End call
     *
     * @param uuid the call uuid
     */
    fun endCall(uuid: Int) {
        val call = SipNotificationManager.getCallById(uuid);
        if (call?.conference?.isIn == true) {
            call.conference?.terminate()

        }
        call?.terminate()
    }

    /**
     * invite a user to a call as conference
     *
     * @param uuid the call uuid
     * @param dialCode the dial code
     * @param name the name if any
     */
    fun inviteToCall(uuid: Int, dialCode: String, name: String? = null) {
        val displayName = name ?: dialCode
        val call = SipNotificationManager.getCallById(uuid)
            ?: throw IllegalArgumentException("Call not found")

        val params = mCore.createCallParams(call)
            ?: throw IllegalArgumentException("Failed to create call params")
        params.mediaEncryption = MediaEncryption.None
        val currentDomain =
            call.remoteAddress.domain ?: throw IllegalArgumentException("Failed to get domain")
        val domain = DeploymentConfig.domainAliases[currentDomain] ?: currentDomain
        throwIfAlreadyInCallOrSelf(dialCode, domain, currentDomain)
        val remoteAddress = Factory.instance().createAddress("sip:$dialCode@$domain")
            ?: throw IllegalArgumentException("Failed to create remote address")
        remoteAddress.displayName = displayName
        if (call.conference == null) {
            mCore.addToConference(call)
        } else if (call.conference?.isIn == false) {
            call.conference?.enter()
        }
        call.conference?.inviteParticipants(arrayOf(remoteAddress), params)
    }

    /**
     * Transfer a call
     *
     * @param uuid the call uuid
     * @param dialCode the dial code
     */
    fun transferCall(uuid: Int, dialCode: String) {
        val call = SipNotificationManager.getCallById(uuid)
            ?: throw IllegalArgumentException("Call not found")
        val domain =
            call.remoteAddress.domain ?: throw IllegalArgumentException("Failed to get domain")
        val translatedDomain = DeploymentConfig.domainAliases[domain]
        throwIfAlreadyInCallOrSelf(dialCode, translatedDomain, domain)
        val remoteAddress =
            Factory.instance().createAddress("sip:$dialCode@${translatedDomain ?: domain}")
                ?: throw IllegalArgumentException("Failed to create remote address")
        call.transferTo(remoteAddress)
    }

    /**
     * destroy the conference and separate all calls
     *
     */
    fun unjoinAllCalls() {
        mCore.calls.forEach {
            it.conference?.leave()
            it.pause()
        }
    }

    /**
     * pause or resume a call
     *
     * @param uuid the call uuid
     * @param pause the pause state true for pause false for resume
     */
    fun pauseCall(uuid: Int, pause: Boolean) {
        SipNotificationManager.getCallById(uuid)?.let {
            if (pause) {
                it.pause()
            } else {
                it.resume()
            }
            PersistentCallStatus.requireUpdate()
        }
    }

    /**
     * check if a call is paused
     *
     * @param uuid the call uuid
     */
    fun getCallPaused(uuid: Int): Boolean {
        val callState = SipNotificationManager.getCallById(uuid)?.state
        return callState == Call.State.Paused || callState == Call.State.Pausing
    }


    private fun throwIfAlreadyInCallOrSelf(
        dialCode: String, translatedDomain: String?, domain: String
    ) {
        if (mCore.calls.find { it.remoteAddress.username == dialCode } != null) {
            throw IllegalArgumentException("Already in a call with $dialCode")
        }
        if (allAccounts.keys.find {
                it.username == dialCode && it.domain == (translatedDomain ?: domain)
            } != null) {
            throw IllegalArgumentException("Cannot call self")
        }
    }

    /**
     * check if a call is in video call
     *
     * @param uuid the call uuid
     */
    fun inVideoCall(uuid: Int): Boolean {
        return SipNotificationManager.getCallById(uuid)?.remoteParams?.isVideoEnabled
            ?: SipNotificationManager.getCallById(uuid)?.params?.isVideoEnabled ?: false
    }

    var microphoneMuted: Boolean
        get() = !mCore.isMicEnabled
        set(value) {
            mCore.isMicEnabled = !value
            PersistentCallStatus.requireUpdate()
        }

    private var remoteCallView: Any?
        get() = mCore.nativeVideoWindowId
        set(value) {
            mCore.nativeVideoWindowId = value
        }

    private var localCallView: Any?
        get() = mCore.nativePreviewWindowId
        set(value) {
            mCore.nativePreviewWindowId = value
        }

    var callViewSupply: Pair<Any?, Any?>
        get() = Pair(remoteCallView, localCallView)
        set(value) {
            if (value.first != null) {
                remoteCallView = value.first
            }
            if (value.second != null) {
                localCallView = value.second
            }
        }
    var cameraSource: String?
        get() = mCore.videoDevice
        set(value) {
            mCore.setVideoDevice(value)
        }

    val cameraSourceList: List<String>
        get() = mCore.videoDevicesList.asList()

    val audioSourceList: List<AudioCommunicationDevice>
        get() {
            mCore.reloadSoundDevices()
            return mCore.audioDevices.mapNotNull {
                translateAudioDeviceInfo(it)
            }
        }

    var audioInputDevice: AudioCommunicationDevice?
        get() {
            return translateAudioDeviceInfo(mCore.inputAudioDevice)
        }
        set(value) {
            mCore.inputAudioDevice = findAudioDevice(value)
        }

    var audioOutputDevice: AudioCommunicationDevice?
        get() {
            return translateAudioDeviceInfo(mCore.outputAudioDevice)
        }
        set(value) {
            mCore.outputAudioDevice = findAudioDevice(value)
        }

    /**
     * Send DTMF action
     *
     * @param dtmf the dtmf to send
     */
    fun sendDTMFAction(dtmf: String) {
        mCore.currentCall?.sendDtmfs(dtmf)
    }


    private fun translateAudioDeviceInfo(device: AudioDevice?): AudioCommunicationDevice? {
        var inputCapable = false
        var outputCapable = false
        device?.let { dev ->
            when (dev.capabilities) {
                AudioDevice.Capabilities.CapabilityAll -> {
                    inputCapable = true
                    outputCapable = true
                }

                AudioDevice.Capabilities.CapabilityRecord -> {
                    inputCapable = true
                }

                AudioDevice.Capabilities.CapabilityPlay -> {
                    outputCapable = true
                }

                else -> {
                    return null
                }
            }
            return AudioCommunicationDevice(dev.id, dev.deviceName, inputCapable, outputCapable)
        }
        return null
    }

    private fun findAudioDevice(device: AudioCommunicationDevice?): AudioDevice? {
        device?.let { dev ->
            return mCore.extendedAudioDevices.firstOrNull { it.id == dev.id }
        }
        return null
    }

    /**
     * Get call logs
     *
     * @return list of call history logs
     */
    val callLogs: List<CallHistoryLog>
        get() {
            return mCore.callLogs.flatMap { log ->
                User.instance.allRegistrations.map { _ ->
                        val callHistoryType = when (log.status) {
                            Call.Status.Success, Call.Status.AcceptedElsewhere -> if (log.dir == Call.Dir.Incoming) CallHistoryType.INCOMING else CallHistoryType.OUTGOING

                            else -> if (log.dir == Call.Dir.Incoming) CallHistoryType.MISSED_INCOMING else CallHistoryType.MISSED_OUTGOING
                        }

                        val numberToDialBack = when (callHistoryType) {
                            CallHistoryType.INCOMING, CallHistoryType.MISSED_INCOMING -> log.fromAddress.username
                            CallHistoryType.MISSED_OUTGOING, CallHistoryType.OUTGOING -> log.toAddress.username
                        }

                        val fromDisplay = getDisplayName(log.fromAddress).trim()
                        val toDisplay = getDisplayName(log.toAddress).trim()
                        val time = Calendar.getInstance().apply {
                            timeInMillis = TimeUnit.SECONDS.toMillis(log.startDate)
                            add(Calendar.YEAR, 1900)
                        }.time

                        CallHistoryLog(
                            fromDisplay,
                            toDisplay,
                            time,
                            log.isVideoEnabled,
                            callHistoryType,
                            numberToDialBack
                        )
                    }
            }
        }


    private fun matchPhoneBookNamesFromAddress(nonNullAddress: Address): String {
        val registrations = User.instance.allRegistrations.filter {
            nonNullAddress.domain?.contains(it.url) == true
        }
        val validName = registrations.firstNotNullOfOrNull { reg ->
            nonNullAddress.username?.let { username ->
                ContactManager.findContactName(username, reg)
            }
        }
        return validName ?: nonNullAddress.displayName ?: nonNullAddress.username ?: "Unknown"
    }

    private fun getDisplayName(address: Address?): String {
        if (address == null) {
            return "Unknown"
        }
        return matchPhoneBookNamesFromAddress(address)
    }

    /**
     * Clean up audio focus and other resources when the engine is destroyed
     */
    fun cleanup() {
        telephonyCallManager.stopMonitoring()
        releaseAudioFocus()
    }
}


