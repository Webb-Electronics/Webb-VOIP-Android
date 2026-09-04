package ca.webb.mobile.companionapp.voip.android.ui.sip.contact.model

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactPhoneBook
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ContactPageVMData(
    val showEdit: Boolean = false,
    val contacts: List<Pair<Contact, ContactPhoneBook>> = emptyList(),
    val errorLoading: Boolean = false,
    val errorDialOut: Boolean = false,
    val detailedContact: Contact? = null,
)

class ContactPageVM : ViewModel() {
    private val _uiState = MutableStateFlow(ContactPageVMData())
    val uiState: StateFlow<ContactPageVMData> = _uiState.asStateFlow()

    var showEdit: Boolean
        get() = _uiState.value.showEdit
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(showEdit = value)
            }
        }
    private var contacts: List<Pair<Contact, ContactPhoneBook>>
        get() = _uiState.value.contacts
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(contacts = value)
            }
        }
    private var errorLoading: Boolean
        get() = _uiState.value.errorLoading
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(errorLoading = value)
            }
        }
    var errorDialOut: Boolean
        get() = _uiState.value.errorDialOut
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(errorDialOut = value)
            }
        }

    var detailedContact: Contact?
        get() = _uiState.value.detailedContact
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(detailedContact = value)
            }
        }

    // helper for android only
    private fun addContacts(pair: Pair<Contact, ContactPhoneBook>) {
        if (this.contacts.none { it == pair }) {
            _uiState.update { currentState ->
                currentState.copy(contacts = currentState.contacts + pair)
            }
        }
    }

    fun reloadContacts() {
        if (!ContactManager.initialized) {
            this.errorLoading = true
            return
        }
        _uiState.update { currentState -> currentState.copy(contacts = emptyList()) }
        for (pb in ContactManager.phoneBooks) {
            for (contact in pb.contacts) {
                addContacts(Pair(contact, pb))
            }
        }
    }

    fun getCurrentList(): List<Contact> {
        //get the contact from the <Contact, ContactPhoneBook> list
        val currentList: List<Contact> = contacts.map { contactPair -> contactPair.first }
        return currentList
    }


    fun getRegistrations(): Pair<List<Registration>, List<Registration>>? {
        contacts.forEach { contact ->
            if (contact.first == this.detailedContact) {
                return contact.second.getAssociatedRegistrations()
            }
        }
        return null
    }

    fun removePreSuffix(str: String): String {
        return str.removePrefix("[").removeSuffix("]")
    }
}


data class ManageContactListSheetVMData(
    val newUrl: String = "",
    val loading: Boolean = false,
    val error: String = "",
    val phoneBooks: List<ContactPhoneBook>? = null,
)

class ManageContactListSheetVM : ViewModel() {
    private val _uiState = MutableStateFlow(ManageContactListSheetVMData())
    val uiState: StateFlow<ManageContactListSheetVMData> = _uiState.asStateFlow()

    var newUrl: String
        get() = _uiState.value.newUrl
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(newUrl = value)
            }
        }
    private var loading: Boolean
        get() = _uiState.value.loading
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(loading = value)
            }
        }
    private var error: String
        get() = _uiState.value.error
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(error = value)
            }
        }
    private var phoneBooks: List<ContactPhoneBook>?
        get() = _uiState.value.phoneBooks
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(phoneBooks = value)
            }
        }

    fun addPhoneBook(context: Context) {
        try {
            ContactManager.addPhoneBook(
                context = context,
                url = this@ManageContactListSheetVM.newUrl
            )
            updateUIVar(phoneBooks = ContactManager.phoneBooks, loading = true)
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            updateUIVar(error = "Error adding phonebook")
        }
        updateUIVar(loading = false)
    }

    fun removePhoneBook(context: Context, pb: ContactPhoneBook) {
        try {
            ContactManager.removePhoneBook(context = context, url = pb.url)
            updateUIVar(removingPhoneBook = pb, loading = true)
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
            updateUIVar(error = "Error removing phonebook")
        }
    }

    private fun updateUIVar(
        loading: Boolean? = null,
        error: String? = null,
        phoneBooks: List<ContactPhoneBook>? = null,
        newURL: String? = null,
        removingPhoneBook: ContactPhoneBook? = null
    ) {
        if (loading != null) {
            this.loading = loading
        }
        if (error != null) {
            this.error = error
        }
        if (phoneBooks != null) {
            this.phoneBooks = phoneBooks.toMutableList()
        }
        if (newURL != null) {
            this.newUrl = newURL
        }
        if (removingPhoneBook != null) {
            val mutableList = this.phoneBooks?.toMutableList()
            mutableList?.remove(removingPhoneBook)
            this.phoneBooks = mutableList?.toList()
        }
    }

    init {
        this.phoneBooks = ContactManager.phoneBooks
    }
}

