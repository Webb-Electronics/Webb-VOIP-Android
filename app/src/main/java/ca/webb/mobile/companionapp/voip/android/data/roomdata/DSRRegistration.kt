package ca.webb.mobile.companionapp.voip.android.data.roomdata

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Upsert
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationData
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol

/**
 * DSR registration
 *
 * @property username the username
 * @property password the password
 * @property url the url
 * @property protocol the protocol
 * @constructor Create an dsr registration
 */
@Entity(primaryKeys = ["username", "url"])
data class DSRRegistration(
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "protocol") val protocol: String
) {
    /**
     *  return registration data from DSRRegistration
     *
     */
    fun toRegistrationData() =
        RegistrationData(url, username, password, SIPTransportProtocol.valueOf(protocol))

    constructor(registrationData: RegistrationData) : this(
        registrationData.username,
        registrationData.password,
        registrationData.url,
        registrationData.transport.name
    )
}

/**
 * Registration dao
 *
 * @constructor to be implemented by subclasses
 */
@Dao
interface RegistrationDao {
    /**
     * Get all registration
     *
     * @return list of registration in DSRRegistration
     */
    @Query("SELECT * FROM DSRRegistration")
    fun getAll(): List<DSRRegistration>

    /**
     * Insert or update registration
     *
     * @param registration the registration to insert or update
     */
    @Upsert
    fun insert(vararg registration: DSRRegistration)

    /**
     * Delete registration
     *
     * @param registration the registration to delete
     */
    @Delete
    fun delete(vararg registration: DSRRegistration)
}