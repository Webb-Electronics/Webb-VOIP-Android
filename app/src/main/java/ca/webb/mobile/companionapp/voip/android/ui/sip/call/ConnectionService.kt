package ca.webb.mobile.companionapp.voip.android.ui.sip.call

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallConnection
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.CallManager

class ConnectionService : ConnectionService() {
    override fun onCreateIncomingConnection(connectionManagerPhoneAccount: PhoneAccountHandle?, request: ConnectionRequest?): Connection {
        val connection = CallConnection()
        connection.setAddress(request?.address, TelecomManager.PRESENTATION_ALLOWED)
        CallManager.currentConnection = connection
        return connection
    }
}