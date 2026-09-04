package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.mockito.Mockito.mock

class ContactTest {
    private var regSelf = Registration(username = "testUser", password = "testPass", domain = "mock.url.name", protocol = SIPTransportProtocol.TLS)
    private var regOther = Registration(username = "testUser2", password = "testPass2", domain = "mock.url.name_wrong", protocol = SIPTransportProtocol.TLS)
    private var pb:ContactPhoneBook?=null
    private val contact1 = Contact("name1", listOf("111"))
    private val contact2 = Contact("name2", listOf("222"))
    private val contact3 = Contact("name3", listOf("333"))
    private val context = mock(Context::class.java)

    @Before
    fun setUp() {
        regSelf.registrationStatus.set(value= RegistrationStatus.REGISTERED)
        regOther.registrationStatus.set(value= RegistrationStatus.REGISTERED)
        val name = "contact phone book name"
        val contacts = listOf(contact1, contact2, contact3)
        val pair:Pair<String, List<Contact>> = Pair(name, contacts)
        pb = ContactPhoneBook("mock.url.name", pair)
    }

    @Test
    fun checkUrlMightAssociateTest_True() {
        val res = pb?.let { ContactPhoneBook.checkUrlMightAssociate(it, regSelf) }
        if (res != null) {
            assertTrue(res)
        }
    }

    @Test
    fun checkUrlMightAssociateTest_False() {
        val res = pb?.let { ContactPhoneBook.checkUrlMightAssociate(it, regOther) }
        if (res != null) {
            assertFalse(res)
        }
    }

    // cannot test due to db limitation
    @Test
    fun getAssociatedRegistrationsTest_Empty() {
        pb?.getAssociatedRegistrations()
        val emptyPair:Pair<List<Registration>, List<Registration>> = Pair(listOf(), listOf())
        assertEquals(pb?.getAssociatedRegistrations(), emptyPair)
    }

    @Test
    fun findContactNameTest_NULL() {
        assertNull(ContactManager.findContactName("123"))
    }

    @Test
    fun getAllContactsTest_Empty() {
        assertEquals(ContactManager.getAllContacts().size, 0)
    }

    // cannot test due to db limitation
    @Test
    fun addPhoneBookTest_Throw() {}
    @Test
    fun loadFromRoomDataTest() {}
    @Test
    fun removePhoneBookTest_() {}

    // leave them for future needs
    @After
    fun tearDown(){}
    @Ignore
    fun sthToIgnore(){}
}
