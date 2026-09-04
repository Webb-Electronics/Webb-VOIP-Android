package ca.webb.mobile.companionapp.voip.android.domain.sip.call

import android.view.TextureView
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.domain.ObjectObserved
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState.CONNECTED
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState.CONNECTING
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState.INCOMING
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.allCalls
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.audioDevice
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.audioDeviceList
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.cameraSource
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.cameraSourceList
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus.micMuted
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager

/**
 * Call state
 *
 * @property CONNECTING connecting
 * @property CONNECTED connected
 * @property INCOMING  incoming
 */
enum class CallState {
    CONNECTING, CONNECTED, INCOMING,
}

/**
 * Call info
 *
 * @property callState the call state
 * @property callingName the name of the caller
 * @constructor Create call info
 */
data class CallInfo(
    val callState: CallState,
    val callingName: String,
)


/**
 * Persistent call status
 *
 * @property allCalls all calls
 * @property micMuted mic muted
 * @property cameraSource camera source
 * @property cameraSourceList camera source list
 * @property audioDevice audio device
 * @property audioDeviceList audio device list
 * @constructor Create persistent call status
 */
object PersistentCallStatus : ObjectObserved() {

    var allCalls = mapOf<Int, CallInfo>()
        set(value) {
            if (twoMapIsEqual(field, value).not()) {
                field = value
                if (value.isEmpty()) {
                    setVideoScreens(null, null)
                }
                requireUpdate()
            }
        }

    var micMuted: Boolean
        get() {
            return LinphoneConnector.engineCoreInstance?.microphoneMuted ?: false
        }
        set(value) {
            LinphoneConnector.engineCoreInstance?.microphoneMuted = value
        }

    var cameraSource: String?
        get() {
            return LinphoneConnector.engineCoreInstance?.cameraSource
        }
        set(value) {
            LinphoneConnector.engineCoreInstance?.cameraSource = value
        }

    val cameraSourceList: List<String>
        get() {
            return LinphoneConnector.engineCoreInstance?.cameraSourceList ?: emptyList()
        }

    var audioDevice: AudioCommunicationDevice?
        get() {
            return LinphoneConnector.engineCoreInstance?.audioOutputDevice
        }
        set(value) {
            if (value!!.inputCapable) LinphoneConnector.engineCoreInstance?.audioInputDevice = value
            if (value.outputCapable) LinphoneConnector.engineCoreInstance?.audioOutputDevice = value
            requireUpdate()
        }

    val audioDeviceList: List<AudioCommunicationDevice>
        get() {
            return LinphoneConnector.engineCoreInstance?.audioSourceList ?: emptyList()
        }


    /**
     * terminate the call with the given uuid
     *
     * @param uuid the uuid of the call
     */
    fun terminateCall(uuid: Int) {
        LinphoneConnector.engineCoreInstance?.endCall(uuid)
    }

    /**
     * send DTMF digit
     *
     * @param digit the digit to send
     */
    fun sendDTMF(digit: String) {
        LinphoneConnector.engineCoreInstance?.sendDTMFAction(digit)
    }

    /**
     * set the video screens
     *
     * @param localView the local view
     * @param remoteView the remote view
     */
    fun setVideoScreens(localView: TextureView?, remoteView: TextureView?) {
        LinphoneConnector.engineCoreInstance?.callViewSupply = Pair(localView, remoteView)
    }

    /**
     * check if the call with the given uuid is a video call
     *
     * @param uuid the uuid of the call
     * @return true if the call is a video call, false otherwise
     */
    fun isVideoCall(uuid: Int): Boolean {
        return LinphoneConnector.engineCoreInstance?.inVideoCall(uuid) ?: false
    }

    /**
     * invite the given digit to the conference
     * @param uuid the uuid of the call
     * @param digit the digit to invite
     */
    fun inviteToConference(uuid: Int, digit: String) {
        try {
            LinphoneConnector.engineCoreInstance?.inviteToCall(
                uuid, digit, ContactManager.findContactName(digit)
            )
        } catch (e: Exception) {
            //TODO: deal with this better
            e.printStackTrace()
        }
    }

    /**
     * transfer the call with the given uuid to the given digit
     *
     * @param uuid the uuid of the call
     * @param digit the digit to transfer to
     */
    fun transferCall(uuid: Int, digit: String) {
        try{
            LinphoneConnector.engineCoreInstance?.transferCall(uuid, digit)
        } catch (e: Exception) {
            //TODO: deal with this better
            e.printStackTrace()
        }
    }

    /**
     * pause the call with the given uuid
     *
     * @param uuid the uuid of the call
     * @param pause true to pause, false to resume
     */
    fun pauseCall(uuid: Int, pause: Boolean) {
        LinphoneConnector.engineCoreInstance?.pauseCall(uuid, pause)
    }

    /**
     * check if the call with the given uuid is paused
     *
     * @param uuid the uuid of the call
     * @return true if the call is paused, false otherwise
     */
    fun getCallPaused(uuid: Int): Boolean {
        return LinphoneConnector.engineCoreInstance?.getCallPaused(uuid) ?: false
    }

    /**
     * pause all ongoing calls
     */
    fun pauseAllOngoingCalls() {
        allCalls.filter { it.value.callState != CallState.INCOMING }.forEach {
            pauseCall(it.key, true)
        }
    }

    /**
     * Get all currently connected calls that are not paused
     */
    fun getActiveConnectedCalls(): List<Int> {
        return allCalls.filter { (uuid, callInfo) ->
            callInfo.callState == CallState.CONNECTED && !getCallPaused(uuid)
        }.keys.toList()
    }

    /**
     * Resume specific calls by UUIDs
     */
    fun resumeCallsByUuid(callUuids: List<Int>) {
        callUuids.forEach { uuid ->
            val callInfo = allCalls[uuid]
            if (callInfo != null && 
                callInfo.callState == CallState.CONNECTED &&
                getCallPaused(uuid)) {
                pauseCall(uuid, false)
            }
        }
    }

    private fun twoMapIsEqual(map1: Map<Int, CallInfo>, map2: Map<Int, CallInfo>): Boolean {
        return map1 == map2 && map1.values == map2.values
    }


}


/**
 * Audio communication device
 *
 * @property id the id
 * @property name the name
 * @property inputCapable input capable
 * @property outputCapable output capable
 * @constructor Create audio communication device
 */
data class AudioCommunicationDevice(
    val id: String,
    val name: String,
    val inputCapable: Boolean,
    val outputCapable: Boolean,
) {
    /**
     * Set audio device to this
     */
    fun setAudio() {
        PersistentCallStatus.audioDevice = this
    }
}
