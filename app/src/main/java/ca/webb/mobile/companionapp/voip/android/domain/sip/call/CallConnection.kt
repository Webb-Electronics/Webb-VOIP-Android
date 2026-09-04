package ca.webb.mobile.companionapp.voip.android.domain.sip.call

import android.telecom.Connection
import android.telecom.DisconnectCause

class CallConnection : Connection() {
    override fun onAnswer() {
        super.onAnswer()
        setActive()
    }

    override fun onDisconnect() {
        super.onDisconnect()
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }
}

object CallManager {
    var currentConnection: CallConnection? = null
    var carrierCallExists: Boolean = false
}