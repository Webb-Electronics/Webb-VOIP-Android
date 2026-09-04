package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model

import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistrationPageVMData(
    val showAddNew: Boolean = false,
    val showingRegistration: Registration? = null,
)
class RegistrationPageVM {
    var user: User =  User.instance

    private val _uiState = MutableStateFlow(RegistrationPageVMData())
    val uiState: StateFlow<RegistrationPageVMData> = _uiState.asStateFlow()

    var showAddNew: Boolean
        get() = _uiState.value.showAddNew
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showAddNew = value)
            }
        }

    var showingRegistration: Registration?
        get() = _uiState.value.showingRegistration
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showingRegistration = value)
            }
        }

}