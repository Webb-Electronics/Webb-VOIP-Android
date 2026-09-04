package ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.MessageSenderData
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MessageVMData(
    var historyMessages: SnapshotStateList<MessageSenderData> = mutableStateListOf()//<MessageSenderData>()
)

class MessageVM(context: Context, localUser: String, remoteUser: String) : ViewModel() {
    private val _uiState = MutableStateFlow(MessageVMData())
    val uiState: StateFlow<MessageVMData> = _uiState.asStateFlow()

    private var historyMessages: SnapshotStateList<MessageSenderData>
        get() = _uiState.value.historyMessages
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(historyMessages = value)
            }
        }

    // manage unhandled received messages if any
    private fun getHistoryMessages(context: Context) {
        PersistentMessage.receivedMessages.forEach { message ->
            var mutableChatList = mutableListOf(message)
            val allChatHistory =
                PersistentMessage.loadAllChatHistory(context, message.localUser, message.remoteUser)
                    ?: emptyList()
            if (allChatHistory.isNotEmpty()) {
                mutableChatList = allChatHistory.toMutableList()
                mutableChatList.add(message)
            }
            PersistentMessage.storeReceivedMessage(
                context,
                message.localUser,
                message.remoteUser,
                mutableChatList
            )
            //PersistentMessage.removeReceivedMessageInTemporaryQueue(message)
            PersistentMessage.requireUpdate()
        }
        while (PersistentMessage.receivedMessages.isNotEmpty()){
            PersistentMessage.receivedMessages.removeAt(PersistentMessage.receivedMessages.lastIndex)
        }
    }

    fun sendMessage(
        context: Context,
        localUser: String,
        remoteUser: String,
        message: String,
        messages: List<MessageSenderData>
    ) {
        val sender = User.instance.allRegistrations.firstOrNull { it.username == localUser }
        sender?.sendMessage(context, localUser, remoteUser, message, messages)
    }

    fun readReceivedMessage(
        context: Context,
        localUser: String,
        remoteUser: String
    ): MessageSenderData? {
        getHistoryMessages(context)
        PersistentMessage.updateMessageReadStatusWithout(context, localUser, remoteUser, true)
        val recentHistoryMessage =
            PersistentMessage.loadAllChatHistory(context, localUser, remoteUser) ?: emptyList()
        if (historyMessages.isEmpty() && recentHistoryMessage.isNotEmpty()) {
            return recentHistoryMessage.last()
        } else if (recentHistoryMessage.isEmpty() || historyMessages.isEmpty()
            || recentHistoryMessage.last() == historyMessages.last()
        ) {
            return null
        }
        return recentHistoryMessage.last()
    }

    init {
        getHistoryMessages(context)
        val tmpHistoryMessages =
            PersistentMessage.loadAllChatHistory(context, localUser, remoteUser) ?: emptyList()
        tmpHistoryMessages.forEach { msg ->
            historyMessages.add(msg)
        }
    }
}

val senderReceiverIdentifier = mapOf(
    false to Alignment.CenterStart,
    true to Alignment.CenterEnd
)