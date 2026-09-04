package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType.INCOMING
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType.MISSED_INCOMING
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType.MISSED_OUTGOING
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType.OUTGOING
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Call history type
 *
 * @property INCOMING incoming
 * @property OUTGOING outgoing
 * @property MISSED_INCOMING missed incoming
 * @property MISSED_OUTGOING missed outgoing
 */
enum class CallHistoryType {
    INCOMING, OUTGOING, MISSED_INCOMING, MISSED_OUTGOING
}

/**
 * Call history log
 *
 * @property displayFrom the display from
 * @property displayTo the display to
 * @property time the time
 * @property isVideo the is video
 * @property callType the call type
 * @property numberToDialBack the number to dial back
 * @property displayLocal the number of the other party depend on whether the call is incoming or outgoing
 * @constructor Create call history log
 */
data class CallHistoryLog(
    val displayFrom: String,
    val displayTo: String,
    val time: Date,
    val isVideo: Boolean,
    val callType: CallHistoryType,
    val numberToDialBack: String?
) {
    val displayLocal: String
        get() {
            if (callType == CallHistoryType.INCOMING || callType == CallHistoryType.MISSED_INCOMING) {
                return displayTo
            }
            return displayFrom
        }

    companion object {
        /**
         * Get parsed call logs
         *
         * @return the list of call history logs
         */
        fun getParsedCallLogs(): List<CallHistoryLog> {
            return LinphoneConnector.engineCoreInstance?.callLogs ?: emptyList()
        }
    }
}

fun Date.formatToDatePattern(pattern: String = "MMM dd,yyyy", locale: Locale = Locale.ENGLISH): String {
    val formatter = SimpleDateFormat(pattern, locale)
    val date = this.clone() as Date
    date.year = date.year - 1900
    return formatter.format(date)
}

fun Date.formatToTimePattern(pattern: String = "hh:mm", locale: Locale = Locale.ENGLISH): String {
    val formatter = SimpleDateFormat(pattern, locale)
    return formatter.format(this)
}