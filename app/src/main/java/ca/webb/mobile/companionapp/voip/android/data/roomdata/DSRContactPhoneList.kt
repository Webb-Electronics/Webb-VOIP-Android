package ca.webb.mobile.companionapp.voip.android.data.roomdata

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Upsert

/**
 * contact phone list
 *
 * @property url the url of the contact phone list
 * @constructor Create DSR object
 */
@Entity
data class DSRContactPhoneList(
    @PrimaryKey val url: String,
)

/**
 * Contact phone list dao
 *
 * @constructor to be implemented by subclasses
 */
@Dao
interface ContactPhoneListDao {
    /**
     * Get all contact phone list
     *
     * @return list of contact phone list in DSRContactPhoneList
     */
    @Query("SELECT * FROM DSRContactPhoneList")
    fun getAll(): List<DSRContactPhoneList>

    /**
     * Update or insert one contact phone list
     *
     * @param phoneList the contact phone list to insert or update
     */
    @Upsert
    fun upsert(vararg phoneList: DSRContactPhoneList)

    /**
     * Delete contact phone list
     *
     * @param contactPhoneList the contact phone list to delete
     */
    @Delete
    fun delete(vararg contactPhoneList: DSRContactPhoneList)
}