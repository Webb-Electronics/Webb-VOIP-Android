package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import ca.webb.mobile.companionapp.voip.android.data.api.NetworkPhoneBookAPI
import ca.webb.mobile.companionapp.voip.android.data.roomdata.WebbRoomDBManager
import ca.webb.mobile.companionapp.voip.android.domain.ObjectObserved

/**
 * Contact
 *
 * @property name the name
 * @property dialTarget the dial target
 * @constructor Create contact
 */
data class Contact(
    val name: String, val dialTarget: List<String>
)

/**
 * Contact phone book
 *
 * @property url the url
 * @property data the data pair
 * @property name the name
 * @property contacts the contacts
 * @constructor Create contact phone book
 */
class ContactPhoneBook(val url: String, data: Pair<String, List<Contact>>) {
    val name: String = data.first
    val contacts: List<Contact> = data.second

    companion object {

        /**
         * Check if the URL might associate with the registration
         *
         * @param checkPhoneBook the check phone book
         * @param registration the registration
         * @return the boolean
         */
        fun checkUrlMightAssociate(
            checkPhoneBook: ContactPhoneBook, registration: Registration
        ): Boolean {
            // check if the host is the same of two URL Strings
            // split the string

            val splitString = registration.url.split("/")
            val toCompareString = if (registration.url.contains("://")) {
                splitString.first()
            } else if (splitString.size > 2) {
                splitString[2]
            } else {
                registration.url
            }.lowercase()
            return checkPhoneBook.url.contains(toCompareString)
        }


    }

    /**
     * Get associated registrations
     *
     * @return the pair of list of registrations, the first list is the registrations that are associated with the phone book, the second list is the registrations that are not associated with the phone book
     */
    fun getAssociatedRegistrations(): Pair<List<Registration>, List<Registration>> {
        val onlineRegistrations = User.instance.allRegistrations.filter {
            it.registrationStatus.get() == RegistrationStatus.REGISTERED
        }
        val sameHostOnlineRegistrations = onlineRegistrations.filter {
            checkUrlMightAssociate(this, it)
        }
        val otherHostOnlineRegistrations = onlineRegistrations - sameHostOnlineRegistrations.toSet()
        return Pair(sameHostOnlineRegistrations, otherHostOnlineRegistrations)
    }


}

/**
 * Contact manager
 *
 * @property phoneBooks the phone books
 * @property initialized the initialized
 * @constructor Singleton object
 */
object ContactManager : ObjectObserved() {
    var phoneBooks: MutableList<ContactPhoneBook> = mutableListOf()
        private set

    var initialized = false
        private set

    /**
     * Load from room data DB
     *
     * @param context the context of the activity
     */
    fun loadFromRoomData(context: Context) {
        val phonebooksUrl = WebbRoomDBManager.retrieveContactPhoneList(context)
        phoneBooks.clear()
        phonebooksUrl.forEach { url ->
            NetworkPhoneBookAPI.retrieveData(context, url) { data ->
                phoneBooks.add(ContactPhoneBook(url, data))
            }
        }
        initialized = true
    }

    /**
     * Add phone book
     *
     * @param context the context of the activity
     * @param url the url
     */
    fun addPhoneBook(context: Context, url: String) {
        val existingPb = phoneBooks.find { it.url == url }
        if (existingPb != null) {
            throw Exception("Phone book already exists")
        }
        NetworkPhoneBookAPI.retrieveData(context, url) { data ->
            phoneBooks.add(ContactPhoneBook(url, data))
            WebbRoomDBManager.addPhoneURL(context, url)
            requireUpdate()
        }
    }

    /**
     * Remove phone book
     *
     * @param context the context of the activity
     * @param url the url
     */
    fun removePhoneBook(context: Context, url: String) {
        phoneBooks.removeIf { it.url == url }
        WebbRoomDBManager.deletePhoneURL(context, url)
    }

    /**
     * Find contact name
     *
     * @param number the number
     * @param reg the registration that to be associated with the phone book
     * @return the string
     */
    fun findContactName(number: String, reg: Registration? = null): String? {
        if (initialized.not()) {
            return null
        }
        var pbs = phoneBooks.toList()
        reg?.let {
            pbs = pbs.filter { pb -> ContactPhoneBook.checkUrlMightAssociate(pb, it) }
        }
        for (pb in pbs) {
            for (contact in pb.contacts) {
                if (contact.dialTarget.contains(number)) {
                    return contact.name
                }
            }
        }
        return null
    }

    /**
     * Get all contacts
     *
     * @return the list of contacts
     */
    fun getAllContacts(): List<Contact> {
        return phoneBooks.flatMap { it.contacts }
    }
}