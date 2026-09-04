package ca.webb.mobile.companionapp.voip.android.data.roomdata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationData
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.LocalRemoteUserData

/**
 * Webb room DB
 *
 * @constructor Create empty Webb room d b
 */
@Database(
    entities = [DSRRegistration::class, DSRContactPhoneList::class, DSRChatHistory::class],
    version = 1
)
abstract class WebbRoomDB : RoomDatabase() {
    /**
     * Registration dao
     *
     * @return generate DAO for registration
     */
    abstract fun registrationDao(): RegistrationDao

    /**
     * Contact phone list dao
     *
     * @return generate DAO for contact phone list
     */
    abstract fun contactPhoneListDao(): ContactPhoneListDao

    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        private const val DB_NAME = "webb_room_db"
        private var instance: WebbRoomDB? = null

        /**
         * Get instance of WebbRoomDB
         *
         * @param context the context of the activity
         * @return instance of WebbRoomDB
         */
        fun getInstance(context: Context): WebbRoomDB {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): WebbRoomDB {
            return Room.databaseBuilder(context, WebbRoomDB::class.java, DB_NAME)
                .allowMainThreadQueries().addMigrations().build()
        }
    }
}

/**
 * Webb room DB manager
 *
 * @constructor NO constructor
 */
class WebbRoomDBManager {
    companion object {
        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */
        fun retrieveMissingRegistrationData(
            context: Context, existingData: MutableList<Registration>
        ) {
            val allRegistrationData = WebbRoomDB.getInstance(context).registrationDao().getAll()
                .map { it.toRegistrationData() }
            allRegistrationData.filter { registrationData ->
                existingData.none {
                    it.url == registrationData.url && it.username == registrationData.username && it.password == registrationData.password && it.protocol == registrationData.transport
                }
            }.forEach { registrationData ->
                existingData.add(Registration(registrationData))
            }
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */
        fun addRegistrationData(context: Context, registration: RegistrationData) {
            WebbRoomDB.getInstance(context).registrationDao().insert(DSRRegistration(registration))
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */

        fun saveRegistrationData(
            context: Context,
            oldRegistrationData: RegistrationData,
            newRegistrationData: RegistrationData
        ) {
            WebbRoomDB.getInstance(context).registrationDao()
                .delete(DSRRegistration(oldRegistrationData))
            WebbRoomDB.getInstance(context).registrationDao()
                .insert(DSRRegistration(newRegistrationData))
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */
        fun deleteRegistrationData(context: Context, registration: RegistrationData) {
            WebbRoomDB.getInstance(context).registrationDao().delete(DSRRegistration(registration))
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */

        fun retrieveContactPhoneList(context: Context): List<String> {
            return WebbRoomDB.getInstance(context).contactPhoneListDao().getAll().map { it.url }
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */

        fun addPhoneURL(context: Context, phoneURL: String) {
            WebbRoomDB.getInstance(context).contactPhoneListDao()
                .upsert(DSRContactPhoneList(phoneURL))
        }

        /**
         * Retrieve all registration data
         *
         * @param context the context of the activity
         * @return list of registration data
         */
        fun deletePhoneURL(context: Context, phoneURL: String) {
            WebbRoomDB.getInstance(context).contactPhoneListDao()
                .delete(DSRContactPhoneList(phoneURL))
        }

        /**
         * add and get, delete MeSsAgE with recipient
         */
        fun createChatRoom(
            context: Context,
            username: String,
            recipient: String,
            messages: String = ""
        ) {
            WebbRoomDB.getInstance(context).chatHistoryDao()
                .upsert(DSRChatHistory(username, recipient, messages))
        }

        fun retrieveChatRecipientList(
            context: Context,
            localUser: String
        ): List<LocalRemoteUserData> {
            return WebbRoomDB.getInstance(context).chatHistoryDao().loadAllByUser(localUser)
                .map { relation ->
                    LocalRemoteUserData(
                        localUser = relation.username,
                        remoteUser = relation.recipient,
                        messages = relation.messages
                    )
                }
        }

        fun retrieveChatMessages (context: Context, username:String, recipient:String): String? {
            return WebbRoomDB.getInstance(context).chatHistoryDao().loadMessages(username, recipient)?.messages
        }

        fun updateMessageReadStatus(
            context: Context,
            username: String,
            recipient: String,
            status: Boolean
        ) {
            WebbRoomDB.getInstance(context).chatHistoryDao()
                .messageReadStatus(username, recipient, status)
        }

        fun getMessageReadStatus (context: Context, username:String, recipient:String):Boolean{
            return WebbRoomDB.getInstance(context).chatHistoryDao().loadMessages(username, recipient)!!.read
        }

        fun deleteChatRoom(context: Context, username: String, recipient: String) {
            WebbRoomDB.getInstance(context).chatHistoryDao().delete(username, recipient)
        }
    }

}