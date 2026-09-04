package ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.model

import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryLog
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CallHistoryPageVMData(
    var callLogs:  List<CallHistoryLog>? =  null,
    var showingCallLog: CallHistoryLog? = null,
)

class CallHistoryPageVM  {
    private val _uiState = MutableStateFlow(CallHistoryPageVMData())
    val uiState: StateFlow<CallHistoryPageVMData> = _uiState.asStateFlow()
    var callLogs: List<CallHistoryLog>?
        get() = _uiState.value.callLogs
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(callLogs = value)
            }
        }
    var showingCallLog: CallHistoryLog?
        get() = _uiState.value.showingCallLog
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showingCallLog = value)
            }
        }
    init {
        this.callLogs = CallHistoryLog.getParsedCallLogs()
    }
}

data class CallHistoryLogListItemVMData(
    var log: CallHistoryLog?=null,
    val errorDial: Boolean = false,
    val showCallSheet: Boolean = false,
)
class CallHistoryLogListItemVM {
    private val _uiState = MutableStateFlow(CallHistoryLogListItemVMData())
    val uiState: StateFlow<CallHistoryLogListItemVMData> = _uiState.asStateFlow()

    var log: CallHistoryLog?
        get() = _uiState.value.log
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(log = value)
            }
        }

    fun getRegistrations():List<Registration> {
        return User.instance.allRegistrations
            .filter{ it.registrationStatus.get() == RegistrationStatus.REGISTERED }
            .toMutableList()
    }
}