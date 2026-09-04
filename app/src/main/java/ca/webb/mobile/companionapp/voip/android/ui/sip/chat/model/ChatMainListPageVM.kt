package ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.MessageSenderData
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChatMainListPageVMData(
    val currentAccount: Registration? = null,
    var rooms: MutableList<LocalRemoteUserData> = mutableListOf(),
)

class ChatMainListPageVM : ViewModel() {
    private val _uiState = MutableStateFlow(ChatMainListPageVMData())
    val uiState: StateFlow<ChatMainListPageVMData> = _uiState.asStateFlow()

    private var currentAccount: Registration?
        get() = User.instance.defaultRegistration
        set(value) {
            _uiState.update { currentState ->
                currentState.copy(currentAccount = value)
            }
            User.instance.setDefaultRegistration(registration = value)
        }

    private val rooms: MutableList<LocalRemoteUserData>
        get() = _uiState.value.rooms

    fun loadAllRecipients(context: Context) {
        val savedRoomList: List<LocalRemoteUserData> =
            PersistentMessage.loadAllRemoteChatUsers(context)
        savedRoomList.forEach { savedRoom ->
            rooms.find { it.getId() == savedRoom.getId() }?.let { currRoom ->
                currRoom.date = savedRoom.date
                currRoom.lastMessage = savedRoom.lastMessage
                currRoom.read = savedRoom.read
            } ?: run { rooms.add(savedRoom) }
        }
        // delete from the list when unregistered
        rooms.removeAll { room ->
            User.instance.allRegistrations.any { reg ->
                reg.registrationStatus.get() == RegistrationStatus.UNREGISTERED && reg.username == room.localUser
            }
        }
        rooms.sortByDescending { it.date }
        handleReceivedMessage(context)
    }

    // received message STORE in the db and then DELETE persistentMessage list
    private fun handleReceivedMessage(context: Context) {
        PersistentMessage.receivedMessages.forEach { message ->
            rooms.find { it.getId() == (message.localUser + message.remoteUser) }?.let { room ->
                var mutableChatList = mutableListOf(message)
                val allChatHistory = PersistentMessage.loadAllChatHistory(
                    context,
                    message.localUser,
                    message.remoteUser
                ) ?: emptyList()
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
                room.date = message.convertStringToDate()
                room.lastMessage = message.message
            } ?: run {
                PersistentMessage.storeReceivedMessage(
                    context,
                    localUser = message.localUser,
                    remoteUser = message.remoteUser,
                    messages = listOf(
                        MessageSenderData(
                            localUser = message.localUser,
                            remoteUser = message.remoteUser,
                            message = message.message,
                            isSender = false
                        )
                    ),
                )
                rooms.add(LocalRemoteUserData(message.localUser, message.remoteUser))
            }
            rooms.firstOrNull { it.localUser == message.localUser && it.remoteUser == message.remoteUser }?.read =
                false
            PersistentMessage.updateMessageReadStatus(
                context,
                message.localUser,
                message.remoteUser,
                false
            )
            PersistentMessage.removeReceivedMessageInTemporaryQueue(message)
        }
    }

    // create chat room based on the default user
    fun createChatRoom(context: Context, remoteUser: String) {
        rooms.find { it.getId() == (currentAccount!!.username + remoteUser) }?.let { _ -> } ?: run {
            rooms.add(LocalRemoteUserData(currentAccount!!.username, remoteUser))
            currentAccount!!.createRoom(context, remoteUser)
        }
        joinRoom(context, currentAccount!!.username, remoteUser)
    }

    fun joinRoom(context: Context, localUser: String, remoteUser: String) {
        PersistentMessage.joinChatRoom(context, localUser, remoteUser)
    }

    fun deleteChatRoom(context: Context, room: LocalRemoteUserData) {
        PersistentMessage.deleteChat(context, room.localUser, room.remoteUser)
        rooms.remove(room)
    }

    fun userRegistered(): Boolean {
        return User.instance.allRegistrations.any { it.registrationStatus.get() == RegistrationStatus.REGISTERED }
    }

    fun lengthyPreviewMessage (previewMessage:String): String {
        return if (previewMessage.length > 20) previewMessage.take(20) + "..."
               else previewMessage
    }

    init {
        if (User.instance.defaultRegistration == null) {
            val registeredUsers = User.instance.allRegistrations.filter {
                it.registrationStatus.get() == RegistrationStatus.REGISTERED
            }
            if (registeredUsers.isNotEmpty()) {
                User.instance.setDefaultRegistration(registration = registeredUsers.first())
            }
        }
        this.currentAccount = User.instance.defaultRegistration
    }
}

data class LocalRemoteUserData(
    val localUser: String,
    val remoteUser: String,
    var date: LocalDateTime? = null,
    var lastMessage: String? = null,
    val messages: String? = null,
    var read: Boolean = false,
    private var id: String? = null
) {
    fun abbreviateDate(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd")
        return date?.format(formatter) ?: "MM DD"
    }

    fun getId(): String {
        return id ?: "no id"
    }

    init {
        id = localUser + remoteUser
    }
}