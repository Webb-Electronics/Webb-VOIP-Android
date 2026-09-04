package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model

import android.content.Context
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactPhoneBook
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationData
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

open class EmptyRegistrationDetailSheetVM {
    protected val currentUser: User = User.instance

    @Suppress("propertyName")
    protected val _uiState = MutableStateFlow(RegistrationData())
    val uiState: StateFlow<RegistrationData> = _uiState.asStateFlow()

    fun register(context: Context) {
        val newData = this.uiState.value
        if (!newData.dataValid()) {
            throw Exception("Invalid data")
        }
        if (currentUser.allRegistrations.any {
                newData.looseEquivalent(it)
            }) {
            throw Exception("Registration already exists")
        }
        val reg = Registration(newData)
        reg.register()
        currentUser.addRegistration(context, registration = reg)
        _uiState.update {
            RegistrationData()
        }
    }

}

class ExistingRegistrationDetailSheetVM(private val registration: Registration) :
    EmptyRegistrationDetailSheetVM() {

    init {
        _uiState.update { RegistrationData(registration) }
    }

    var newUrl: String
        get() = _uiState.value.url
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(url = value)
            }
        }
    var newUsername: String
        get() = _uiState.value.username
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(username = value)
            }
        }
    var newPassword: String
        get() = _uiState.value.password
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(password = value)
            }
        }
    var newTransport: SIPTransportProtocol
        get() = _uiState.value.transport
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(transport = value)
            }
        }

    fun save(context: Context) {
        if (currentUser.allRegistrations.find {
                _uiState.value.equivalent(it)
            } != null) {
            throw Exception("Registration already exists")
        }
        registration.updateWithNewData(context, _uiState.value)
    }

    fun remove(context: Context) {
        currentUser.removeRegistration(context, registration)
    }

    fun getContact(reg: Registration): List<Contact> {
        return ContactManager.phoneBooks.filter { ContactPhoneBook.checkUrlMightAssociate(checkPhoneBook = it, registration = reg) }.flatMap {
            it.contacts
        }

    }
}

