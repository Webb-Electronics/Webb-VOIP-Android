package ca.webb.mobile.companionapp.voip.android.domain.sip.message

import android.content.Context
import android.content.Intent
import android.util.Log
import ca.webb.mobile.companionapp.voip.android.data.roomdata.WebbRoomDBManager
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector
import ca.webb.mobile.companionapp.voip.android.domain.ObjectObserved
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage.receivedMessages
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus.REGISTERED
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.User
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.MessageActivity
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.LocalRemoteUserData
import com.google.gson.Gson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * MessageSenderData
 *
 * @property localUser sender
 * @property remoteUser recipient
 * @property date date of message sent (=when this created)
 * @property message text message to send
 * @property isSender true if sender else false
 * @constructor Create message and its info
 */
data class MessageSenderData(
    val localUser: String,
    val remoteUser: String,
    var date: String? = null,
    val message: String,
    val isSender: Boolean,
) {
    private fun updateNewDateTime() {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        date = currentDateTime.format(formatter)
    }

    fun convertStringToDate(): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.parse(date, formatter)
    }

    init {
        updateNewDateTime()
    }
}

/**
 * Persistent received message
 *
 * @property receivedMessages is a temporary list stores received message that is deleted when stored in local db (Room):
when a message is received, the message and its info will be placed in the list.
The message in the list will be read
by either ChatMainListPageVM (Main Activity) or MessageVM(Message Activity).
They will store it to the local db (room) and then delete the message in the list.
Deletion is inevitable since two different activities are trying
to read and store the same value to the db, which is duplicated behavior.
 * @constructor Create persistent received message
 */

object PersistentMessage : ObjectObserved() {

    val receivedMessages: MutableList<MessageSenderData> = mutableListOf()

    /**
     * add received message to receivedMessages queue
     *
     * @param localUser the registered user
     * @param remoteUser the message sender
     * @param message the message received
     */
    fun messageReceiving(localUser: String, remoteUser: String, message: String?) {
        receivedMessages.add(
            MessageSenderData(
                localUser = localUser, remoteUser = remoteUser,
                message = message!!, isSender = false
            )
        )
        requireUpdate()
    }

    /**
     * remove received message in the receivedMessages queue after stored in db
     * need to render twice
     *
     * @param message the message received
     */
    fun removeReceivedMessageInTemporaryQueue(message: MessageSenderData) {
        receivedMessages.remove(message)
    }

    /**
     * update whether message is read status when user read the message
     *
     * @param localUser the registered user
     * @param remoteUser the message sender
     * @param status true if read else false
     */
    fun updateMessageReadStatus(
        context: Context,
        localUser: String,
        remoteUser: String,
        status: Boolean
    ) {
        WebbRoomDBManager.updateMessageReadStatus(
            context,
            username = localUser,
            recipient = remoteUser,
            status = status
        )
        requireUpdate()
    }

    /**
     * update whether message is read status when user read the message w/o re-render
     *
     * @param localUser the registered user
     * @param remoteUser the message sender
     * @param status true if read else false
     */
    fun updateMessageReadStatusWithout(
        context: Context,
        localUser: String,
        remoteUser: String,
        status: Boolean
    ) {
        WebbRoomDBManager.updateMessageReadStatus(
            context,
            username = localUser,
            recipient = remoteUser,
            status = status
        )
    }

    /**
     * returns current chat room's message read status
     *
     * @param context context from the composable
     * @param localUser the registered user
     * @param remoteUser the message sender
     */
    private fun getMessageReadStatus(
        context: Context,
        localUser: String,
        remoteUser: String
    ): Boolean {
        return WebbRoomDBManager.getMessageReadStatus(
            context,
            username = localUser,
            recipient = remoteUser,
        )
    }

    /**
     * returns the list of saved recipient users based on registered accounts
     *
     * @param context context from the composable
     */
    fun loadAllRemoteChatUsers(context: Context): List<LocalRemoteUserData> {
        val allRemoteUsers: MutableList<LocalRemoteUserData> = mutableListOf()
        val registeredUsers =
            User.instance.allRegistrations.filter { it.registrationStatus.get() == REGISTERED }
        registeredUsers.forEach { user ->
            WebbRoomDBManager.retrieveChatRecipientList(context, user.username)
                .filter { !it.messages.isNullOrBlank() }
                .forEach { remote ->

                    val deserializedMessage =
                        Gson().fromJson(remote.messages, Array<MessageSenderData>::class.java)
                            .toList().last()
                    allRemoteUsers.add(
                        LocalRemoteUserData(
                            localUser = user.username,
                            remoteUser = remote.remoteUser,
                            date = deserializedMessage.convertStringToDate(),
                            lastMessage = deserializedMessage.message,
                            read = getMessageReadStatus(
                                context = context,
                                localUser = user.username,
                                remoteUser = remote.remoteUser
                            )
                        )
                    )

                }
        }
        return allRemoteUsers
    }

    /**
     * join to the message activity
     *
     * @param context context from the composable
     * @param localUser the registered user
     * @param remoteUser the message sender
     */
    fun joinChatRoom(context: Context, localUser: String, remoteUser: String) {
        val intentMsg = Intent(context, MessageActivity::class.java)
        intentMsg.putExtra("localUser", localUser)
        intentMsg.putExtra("remoteUser", remoteUser)
        intentMsg.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        context.startActivity(intentMsg)
    }

    /**
     * load all chat history between local and remote users
     *
     * @param context context from the composable
     * @param localUser the registered user
     * @param remoteUser the message sender
     */
    fun loadAllChatHistory(
        context: Context,
        localUser: String,
        remoteUser: String
    ): List<MessageSenderData>? {
        val serializedMessage =
            WebbRoomDBManager.retrieveChatMessages(context, localUser, remoteUser)
        if (!serializedMessage.isNullOrBlank()) {
            return (Gson().fromJson(serializedMessage, Array<MessageSenderData>::class.java)
                .toList())
        }
        return null
    }

    /**
     * store message to db
     *
     * @param context context from the composable
     * @param localUser the registered user
     * @param remoteUser the message sender
     * @param messages messages to store
     */
    fun storeReceivedMessage(
        context: Context,
        localUser: String,
        remoteUser: String,
        messages: List<MessageSenderData>
    ) {
        val deserializeMessage = Gson().toJson(messages)
        WebbRoomDBManager.createChatRoom(context, localUser, remoteUser, deserializeMessage)
    }

    /**
     * delete the chat room
     *
     * @param context context from the composable
     * @param localUser the registered user
     * @param remoteUser the message sender
     */
    fun deleteChat(context: Context, localUser: String, remoteUser: String) {
        try {
            WebbRoomDBManager.deleteChatRoom(context, localUser, remoteUser)
            LinphoneConnector.engineCoreInstance?.deleteChatRoom(
                localUser = localUser,
                remoteUser = remoteUser
            )
            receivedMessages.remove(receivedMessages.first { it.localUser == localUser && it.remoteUser == remoteUser })
        } catch (error: Exception) {
            Log.e("Delete Chat Error", error.message.toString())
        }
    }

}