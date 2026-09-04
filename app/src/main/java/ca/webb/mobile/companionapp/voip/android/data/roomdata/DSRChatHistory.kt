package ca.webb.mobile.companionapp.voip.android.data.roomdata

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Upsert


@Entity(primaryKeys = ["username", "recipient"])
data class DSRChatHistory(
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "recipient") val recipient: String,
    @ColumnInfo(name = "messages") val messages: String?,
    @ColumnInfo(name = "read") val read: Boolean = true,
)

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM DSRChatHistory")
    fun getAll(): List<DSRChatHistory>

    @Query("SELECT * FROM DSRChatHistory WHERE (username IN (:username) AND (recipient IN (:recipient)))")
    fun loadMessages(username: String, recipient:String): DSRChatHistory?

    @Query("SELECT * FROM DSRChatHistory WHERE username IN (:username)")
    fun loadAllByUser(username: String): List<DSRChatHistory>

    @Query("SELECT * FROM DSRChatHistory WHERE (username IN (:username) AND (recipient IN (:recipient)))")
    fun isRelationExist(username: String, recipient: String): DSRChatHistory?

    @Query("UPDATE DSRChatHistory SET read = :readingStatus WHERE (username IN (:username) AND (recipient IN (:recipient)))")
    fun messageReadStatus(username: String, recipient: String, readingStatus: Boolean)

    @Upsert
    fun upsert(vararg chatHistory: DSRChatHistory)

    @Query("DELETE FROM DSRChatHistory WHERE (username IN (:username) AND (recipient IN (:recipient)))")
    fun delete(username: String, recipient: String)
}