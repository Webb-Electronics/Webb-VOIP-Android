package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.mockito.Mockito.mock

class RegistrationTest {
    private var reg : Registration? = null
    private var regData : RegistrationData = RegistrationData()
    private val context = mock(Context::class.java)

    @Before
    fun setUp() {
        reg = Registration(username = "testUser", password = "testPass", domain = "nothing", protocol = SIPTransportProtocol.TLS)
    }

    @Test
    fun registerTest_Throw() {
        reg?.registrationStatus?.set(value= RegistrationStatus.REGISTERED)
        var throwing = ThrowingRunnable{reg?.register()}
        assertThrows(IllegalStateException::class.java, throwing )
    }

    @Test
    fun unregisterTest() {
        reg?.register()
        assertNotNull(reg?.sipAccount)
        reg?.unregister()
        assertNull(reg?.sipAccount)
    }

    @Test
    fun setAsDefaultTest_Throw() {
        reg?.registrationStatus?.set(value= RegistrationStatus.UNREGISTERED)
        var throwing = ThrowingRunnable{reg?.setAsDefault()}
        assertThrows(IllegalStateException::class.java, throwing )
    }

    @Test
    fun dialTest_Throw() {
        reg?.registrationStatus?.set(value= RegistrationStatus.UNREGISTERED)
        var throwing = ThrowingRunnable{reg?.dial(context, "123")}
        assertThrows(IllegalStateException::class.java, throwing )
    }

    @Test
    fun updateWithNewData_Registered_Throw() {
        reg?.registrationStatus?.set(value= RegistrationStatus.REGISTERED)
        var throwing = ThrowingRunnable{reg?.updateWithNewData(context, regData)}
        assertThrows(IllegalStateException::class.java, throwing )
    }

    @Test
    fun updateWithNewData_Empty_Throw() {
        reg?.registrationStatus?.set(value= RegistrationStatus.UNREGISTERED)
        var throwing = ThrowingRunnable{reg?.updateWithNewData(context, regData)}
        assertThrows(IllegalArgumentException::class.java, throwing )
    }


    // TODO: These tests were to test whether each functionality working as expected
    // However, LinphoneConnector's engineCoreInstance should not be null
    // so we need to pass ComponentActivity to createAnd...() like MainActivity
    // , but could not figured out how to do on the development machine.
    // private val test = mock(ComponentActivity::class.java) // could not used since "not-mocked" issue
    //
    // These tests will be moved to tests on an Android device.
    @Test
    fun registerTest_Success() {}
    @Test
    fun setAsDefaultTest_Success() {}
    @Test
    fun dialTest_Success() {}
    @Test
    fun createSIPAccountWithoutRegistration_Success() {}
    @Test
    fun updateWithNewData_Success() {}

    // leave them for future needs
    @After
    fun tearDown(){}
    @Ignore
    fun sthToIgnore(){}
}