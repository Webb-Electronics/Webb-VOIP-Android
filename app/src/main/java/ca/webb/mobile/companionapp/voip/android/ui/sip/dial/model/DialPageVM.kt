package ca.webb.mobile.companionapp.voip.android.ui.sip.dial.model

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.BuildConfig
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.SipNotificationManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.ConnectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DialPageVMData(
    val onlineRegistration: List<Registration> = emptyList(),
    val numberToDial: String = "",
    val currentAccount: Registration? = null,
    val audioOnly: Boolean = false,
)

class DialPageVM : ViewModel() {
    private val _uiState = MutableStateFlow(DialPageVMData())
    val uiState: StateFlow<DialPageVMData> = _uiState.asStateFlow()

    var onlineRegistration: List<Registration>
        get() = User.instance.allRegistrations.filter {
            it.registrationStatus.get() == RegistrationStatus.REGISTERED
        }
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(onlineRegistration = value)
            }
            User.instance.allRegistrations.filter {
                it.registrationStatus.get() == RegistrationStatus.REGISTERED
            }
        }
    var numberToDial: String
        get() = _uiState.value.numberToDial
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(numberToDial = value)
            }
        }
    var currentAccount: Registration?
        get() = User.instance.defaultRegistration
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(currentAccount = value)
            }
            User.instance.setDefaultRegistration(registration = value)
        }

    fun getOnlyAudioCall(context: Context): Boolean {
        return User.getOnlyAudioCall(context)
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

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun dial(context: Context) {
        //TODO (Yosup): can i use the data layer's data here?
        if (SipNotificationManager.getOnCallStatus) {
            Toast.makeText(
                context, "You cannot call while in a call", Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (onlineRegistration.isEmpty()) {
            Toast.makeText(
                context, "You need at least 1 account that is registered", Toast.LENGTH_SHORT
            ).show()
            return
        }
        // if gsm call is ongoing, outgoing voip call to be system level call
        if (CallManager.carrierCallExists){
            try {
                val telecomManager =
                    context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
                val handle = PhoneAccountHandle(
                    ComponentName(context, ConnectionService::class.java),
                    BuildConfig.UNIQUE_VOIP_ID
                )
                val uri = Uri.fromParts("sip", BuildConfig.UNIQUE_CALLER_ID, null)
                val extras = Bundle().apply {
                    putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, handle)
                }
                telecomManager.placeCall(uri, extras)
                CallManager.currentConnection?.setDialing()
                //CallManager.currentConnection?.setActive()
            } catch (e: Exception) {
                println(e)
            }
        }

        currentAccount?.dial(context, numberToDial)
        numberToDial = ""
    }

    fun pasteNumberOnlyFromClipboard(context: Context, external: Boolean) {
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val item = clip.getItemAt(0)
            val pastedText = item.text.toString()
            numberToDial =
                if (external) "9${pastedText.replace("\\D".toRegex(), "")}" else pastedText.replace(
                    "\\D".toRegex(),
                    ""
                )
        }
    }

    init {
        this.numberToDial = ""
        this.onlineRegistration = User.instance.allRegistrations.filter {
            it.registrationStatus.get() == RegistrationStatus.REGISTERED
        }
        this.currentAccount = User.instance.defaultRegistration
    }

}