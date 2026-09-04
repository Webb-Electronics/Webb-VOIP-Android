package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.function.ThrowingRunnable
import org.mockito.Mockito.mock
import java.util.Date

class CallHistoryLogTest {
    private var reg : Registration? = null
    private val dateTemp = Date()
    private var callLog_to  =
        CallHistoryLog(displayFrom = "from_mock",
            displayTo = "to_mock",
            time = dateTemp,
            isVideo = true,
            callType = CallHistoryType.INCOMING,
            numberToDialBack = "back_mock")
    private var callLog_from  =
        CallHistoryLog(displayFrom = "from_mock",
            displayTo = "to_mock",
            time = dateTemp,
            isVideo = true,
            callType = CallHistoryType.OUTGOING,
            numberToDialBack = "back_mock")

    @Before
    fun setUp() {
        reg = Registration(username = "testUser", password = "testPass", domain = "nothing", protocol = SIPTransportProtocol.TLS)
    }

    @Test
    fun displayLocalTest_To (){
        assertEquals(callLog_to.displayLocal, "to_mock")
    }

    @Test
    fun displayLocalTest_From (){
        assertEquals(callLog_from.displayLocal, "from_mock")
    }

    @Test
    fun getParsedCallLogsTest_Empty() {
        val emptyList: List<CallHistoryLog> = listOf()
        assertEquals(CallHistoryLog.getParsedCallLogs(), emptyList)
    }

    // leave them for future needs
    @After
    fun tearDown(){}
    @Ignore
    fun sthToIgnore(){}

}