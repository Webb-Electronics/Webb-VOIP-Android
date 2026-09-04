package ca.webb.mobile.companionapp.voip.android.ui.sip.call.model

import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallState
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CurrentApplicationDisplayState {
    NOT_IN_CALL, CONNECTING_CALL, IN_CALL
}

data class CallContainerVMData(
    val callPageVM: CallPageVM? = null,
    val allVM: List<CallPageVM> = emptyList(),
)

class CallContainerVM : ViewModel() {
    private val _uiState = MutableStateFlow(CallContainerVMData())
    val uiState: StateFlow<CallContainerVMData> = _uiState.asStateFlow()
    var callPageVM: CallPageVM?
        get() = _uiState.value.callPageVM
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(callPageVM = value)
            }
        }

    val isVideoCall: Boolean
        get() {
            if (callPageVM == null) {
                return false
            }
            return PersistentCallStatus.isVideoCall(callPageVM!!.callingUUID)
        }

    var allVM: List<CallPageVM>
        get() = _uiState.value.allVM.toList()
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(allVM = value)
            }
        }

    fun getCurrentPageState(): CurrentApplicationDisplayState {
        // sync allVM with PersistentCallStatus
        syncAllVM()
        if (callPageVM != null) {
            return when (PersistentCallStatus.allCalls[callPageVM!!.callingUUID]?.callState) {
                CallState.CONNECTING -> CurrentApplicationDisplayState.CONNECTING_CALL
                CallState.CONNECTED -> CurrentApplicationDisplayState.IN_CALL
                else -> {
                    CurrentApplicationDisplayState.NOT_IN_CALL
                }
            }
        }

        // check if there is a connecting call, if so return CONNECTING_CALL and set the callPageVM to the first connecting call
        val connectingCall = allVM.find {
            it.uiState.value.callingUUID == PersistentCallStatus.allCalls.keys.find { cs -> PersistentCallStatus.allCalls[cs]?.callState == CallState.CONNECTING }
        }
        if (connectingCall != null) {
            callPageVM = connectingCall
            return CurrentApplicationDisplayState.CONNECTING_CALL
        }

        // check if there is a call in progress, if so return IN_CALL and set the callPageVM to the first call in progress
        val inCall = allVM.find {
            it.uiState.value.callingUUID == PersistentCallStatus.allCalls.keys.find { cs -> PersistentCallStatus.allCalls[cs]?.callState == CallState.CONNECTED }
        }
        if (inCall != null) {
            callPageVM = inCall
            return CurrentApplicationDisplayState.IN_CALL
        }

        // if there are no calls in progress or connecting, return NOT_IN_CALL
        callPageVM = null
        return CurrentApplicationDisplayState.NOT_IN_CALL
    }

    fun updateCallingVM(callPageVM: CallPageVM) {
        if (callPageVM.callingUUID == this.callPageVM?.callingUUID) {
            return
        }
        this.callPageVM = callPageVM
        PersistentCallStatus.requireUpdate()
    }

    private fun syncAllVM() {
        val listOfUUIDCurrent = allVM.map { it.callingUUID }
        val listOfUUIDToSync = PersistentCallStatus.allCalls.filter {
            it.value.callState != CallState.INCOMING
        }.keys.toList()
        val toRemove = listOfUUIDCurrent.filter { !listOfUUIDToSync.contains(it) }
        val toAdd = listOfUUIDToSync.filter { !listOfUUIDCurrent.contains(it) }
        val mutableAllVM = allVM.toMutableList()
        for (removingId in toRemove) {
            mutableAllVM.removeAll { it.callingUUID == removingId }
        }
        for (addingId in toAdd) {
            mutableAllVM.add(CallPageVM(addingId))
        }
        allVM = mutableAllVM.toList()
        if (allVM.contains(callPageVM).not()) {
            callPageVM = null
        }
    }

}