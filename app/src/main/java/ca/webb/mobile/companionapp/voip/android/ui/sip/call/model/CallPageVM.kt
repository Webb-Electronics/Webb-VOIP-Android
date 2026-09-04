package ca.webb.mobile.companionapp.voip.android.ui.sip.call.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SpeakerPhone
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.ui.graphics.vector.ImageVector
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.AudioCommunicationDevice
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

private val AUDIO_DEVICE_SYSTEM_ICONS = mapOf(
    "Device" to Icons.Filled.PhoneAndroid,
    "Speaker" to Icons.Filled.SpeakerPhone,
    "Bluetooth" to Icons.Filled.Bluetooth
)
private val AUDIO_DEVICE_NAMES = mapOf(
    "Device" to "Android Standard",
    "Speaker" to "Android Speaker",
    "Bluetooth" to "Bluetooth"
)
private val CAMERA_DEVICE_SYSTEM_ICONS = mapOf(
    "FrontFacingCamera" to Icons.Filled.Person,
    "BackFacingCamera" to Icons.Filled.FlipCameraAndroid,
    "StaticImage: Static picture" to Icons.Filled.VideocamOff
)
private val CAMERA_DEVICE_NAMES = mapOf(
    "FrontFacingCamera" to "Front Camera",
    "BackFacingCamera" to "Back Camera",
    "StaticImage: Static picture" to "Camera Off"
)

data class CallPageVMData(
    val callingUUID: Int, val callingText: String, val startTime: Date
)

class CallPageVM(uuid: Int) {
    private val _uiState: MutableStateFlow<CallPageVMData> = MutableStateFlow(
        CallPageVMData(
            callingUUID = uuid,
            callingText = PersistentCallStatus.allCalls[uuid]?.callingName ?: "Unknown",
            startTime = Date()
        )
    )
    val uiState: StateFlow<CallPageVMData> = _uiState.asStateFlow()


    var callingUUID: Int
        get() = _uiState.value.callingUUID
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(callingUUID = value)
            }
        }

    var callingText: String
        get() = _uiState.value.callingText
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(callingText = value)
            }
        }

    var startTime: Date
        get() = _uiState.value.startTime
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(startTime = value)
            }
        }

    fun endCall() {
        PersistentCallStatus.terminateCall(callingUUID)
    }
}

data class AudioCallPageVMData(
    val showDialPad: Boolean,
    val showConferenceInviteSheet: Boolean,
    val showCallTransferSheet: Boolean,
    val errorDialCode: Boolean,
    val dtmfToSend: String
)

open class AudioCallPageVM(
    callPageVM: CallPageVM,
    private val allVMs: List<CallPageVM>,
    private val updater: (CallPageVM) -> Unit
) {
    private val _uiState = MutableStateFlow(
        AudioCallPageVMData(
            showDialPad = false,
            showConferenceInviteSheet = false,
            showCallTransferSheet = false,
            errorDialCode = false,
            dtmfToSend = ""
        )
    )
    val uiState: StateFlow<AudioCallPageVMData> = _uiState.asStateFlow()
    var callPageVM: CallPageVM = callPageVM
        private set

    var showConferenceInviteSheet: Boolean
        get() = _uiState.value.showConferenceInviteSheet
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showConferenceInviteSheet = value)
            }
        }

    var dtmfToSend: String
        get() = _uiState.value.dtmfToSend
        set(value) {
            if (value.count() > dtmfToSend.count()) {
                PersistentCallStatus.sendDTMF(value.last().toString())
            }
            _uiState.update { currentState ->
                currentState.copy(dtmfToSend = value)
            }
        }

    var paused: Boolean
        get() = PersistentCallStatus.getCallPaused(callPageVM.callingUUID)
        set(value) {
            PersistentCallStatus.pauseCall(callPageVM.callingUUID, value)
        }

    var showDialPad: Boolean
        get() = _uiState.value.showDialPad
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showDialPad = value)
            }
        }

    var currentUUID: Int
        get() = callPageVM.callingUUID
        set(value) {
            callPageVM = allVMs.find { it.callingUUID == value } ?: callPageVM
            updater(callPageVM)
        }

    val allUUIDs: List<Int>
        get() = allVMs.map { it.callingUUID }

    var micMuted: Boolean
        get() = PersistentCallStatus.micMuted
        set(value) {
            PersistentCallStatus.micMuted = value
        }

    val availableAudioDevices: List<AudioCommunicationDevice>
        get() = PersistentCallStatus.audioDeviceList

    var currentAudioDevice: AudioCommunicationDevice?
        get() = PersistentCallStatus.audioDevice
        set(value) {
            value?.setAudio()
        }

    val allContacts: List<Contact>
        get() = ContactManager.getAllContacts()

    fun inviteToConference(digit: String) {
        PersistentCallStatus.inviteToConference(callPageVM.callingUUID, digit)
    }

    fun transferCall(digit: String) {
        PersistentCallStatus.transferCall(callPageVM.callingUUID, digit)
    }

    fun queryCallPaused(uuid: Int) {
        PersistentCallStatus.getCallPaused(uuid)
    }

    companion object {
        fun getCallerName(uuid: Int): String {
            return PersistentCallStatus.allCalls[uuid]?.callingName ?: "Unknown"
        }


        fun translateAudioDeviceName(audioDeviceType: List<AudioCommunicationDevice>): MutableList<Pair<AudioCommunicationDevice?, String>> {
            val res:MutableList<Pair<AudioCommunicationDevice?, String>> = emptyList<Pair<AudioCommunicationDevice?, String>>().toMutableList()
            audioDeviceType.forEach { adt ->
                val name: String = adt.id.split(":")[0]
                if (name.lowercase().contains("earpiece")){
                    res.add(Pair(adt, "Device"))
                } else if (name.lowercase().contains("speaker")){
                    res.add(Pair(adt, "Speaker"))
                } else if (name.lowercase().contains("bluetooth")
                    && !name.lowercase().contains("capture")){
                    res.add(Pair(adt, "Bluetooth"))
                }else{
                    res.add(Pair(adt, name))
                }
            }
            return res
        }

        fun imageAudioDeviceName(name: String): ImageVector {
            return AUDIO_DEVICE_SYSTEM_ICONS[name] ?: Icons.Filled.Cable
        }
    }


}


class VideoCallPageVM(
    callPageVM: CallPageVM, allVMs: List<CallPageVM>, updater: (CallPageVM) -> Unit
) : AudioCallPageVM(callPageVM, allVMs, updater) {

    var currentCameraDevice: String?
        get() = PersistentCallStatus.cameraSource
        set(value) {
            PersistentCallStatus.cameraSource = value
        }
    val availableCamera: List<String>
        get() = PersistentCallStatus.cameraSourceList

    companion object {
        fun translateCameraDeviceName(cameraDeviceType: String): String {
            return CAMERA_DEVICE_NAMES[cameraDeviceType] ?: cameraDeviceType
        }

        fun imageCameraDeviceName(cameraDeviceType: String): ImageVector {
            return CAMERA_DEVICE_SYSTEM_ICONS[cameraDeviceType] ?: Icons.Filled.Cable
        }
    }

}
