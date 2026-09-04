package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model

import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.linphone.mediastream.MediastreamerAndroidContext.getContext

data class GeneralSettingsVMData(
    val registrationDefault: Registration? = null,
    val onlyAudioCall: Boolean = false,
    val autoCameraOff: Boolean = false,
)

class GeneralSettingsVM : ViewModel() {
    private val _uiState = MutableStateFlow(GeneralSettingsVMData())
    val uiState: StateFlow<GeneralSettingsVMData> = _uiState.asStateFlow()
    val registrations: List<Registration>
        get() {
            return User.instance.allRegistrations.filter {
                it.registrationStatus.get() == RegistrationStatus.REGISTERED
            }
        }


    var registrationDefault: Registration?
        get() = User.instance.defaultRegistration
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(registrationDefault = value)
            }
            User.instance.setDefaultRegistration(registration = value)
        }

    var onlyAudioCall: Boolean
        get() = _uiState.value.onlyAudioCall
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(onlyAudioCall = value)
            }
            User.setOnlyAudioCall(getContext(), value)
        }
    var autoCameraOff: Boolean
        get() = User.getAutoCameraOff(getContext())
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(autoCameraOff = value)
            }
            User.setAutoCameraOff(getContext(), value)
            if (User.getAutoCameraOff(getContext())) {
                PersistentCallStatus.cameraSource = "StaticImage: Static picture"
            } else {
                PersistentCallStatus.cameraSource = "FrontFacingCamera"
            }
        }

    fun defaultUser() {
        if (User.instance.defaultRegistration == null) {
            val registeredUsers = User.instance.allRegistrations.filter {
                it.registrationStatus.get() == RegistrationStatus.REGISTERED
            }
            if (registeredUsers.isNotEmpty()) {
                User.instance.setDefaultRegistration(registration = registeredUsers.first())
            }
        }
    }

    init {
        this.onlyAudioCall = User.getOnlyAudioCall(getContext())
        this.autoCameraOff = User.getAutoCameraOff(getContext())
        this.registrationDefault = User.instance.defaultRegistration
    }
}