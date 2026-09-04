package ca.webb.mobile.companionapp.voip.android.domain.sip.user

import android.content.Context
import ca.webb.mobile.companionapp.voip.android.data.roomdata.WebbRoomDBManager
import ca.webb.mobile.companionapp.voip.android.data.sharedpreference.SharedPreferenceConfigurator
import ca.webb.mobile.companionapp.voip.android.data.sip.linphone.LinphoneConnector


/**
 * User class for handling user data
 *
 * @property allRegistrations all registrations
 * @property defaultRegistration default registration
 * @constructor Singleton user, use with instance
 */
class User private constructor(registrations: MutableList<Registration>) {
    var allRegistrations: MutableList<Registration> = registrations
        private set

    var defaultRegistration: Registration? = null
        private set

    /**
     * Remove registration
     *
     * @param context the context
     * @param registration the registration
     */
    fun removeRegistration(context: Context, registration: Registration) {
        allRegistrations.remove(registration)
        WebbRoomDBManager.deleteRegistrationData(context, RegistrationData(registration))
    }

    /**
     * Add registration
     *
     * @param context the context
     * @param registration the registration
     */
    fun addRegistration(context: Context, registration: Registration) {
        allRegistrations.add(registration)
        WebbRoomDBManager.addRegistrationData(context, RegistrationData(registration))
    }

    /**
     * Set default registration
     *
     * @param registration the registration
     */
    fun setDefaultRegistration(registration: Registration?) {
        try {
            registration?.setAsDefault()
        } catch (_: Exception) {

        }
        defaultRegistration = registration
    }

    companion object {
        val instance: User = User(
            LinphoneConnector.engineCoreInstance?.getCurrentRegistrations() ?: mutableListOf()
        )

        /**
         * Fill missing registration from DB
         *
         * @param context the context
         */
        fun fillMissingRegistrationFromDB(context: Context) {
            WebbRoomDBManager.retrieveMissingRegistrationData(context, instance.allRegistrations)
        }

        /**
         * check if camera is auto off
         *
         * @param context the context
         * @return the boolean
         */
        fun getAutoCameraOff(context: Context): Boolean =
            SharedPreferenceConfigurator(context).cameraAutoOff

        /**
         * set auto camera off
         *
         * @param context the context
         * @param value the value
         */
        fun setAutoCameraOff(context: Context, value: Boolean) {
            SharedPreferenceConfigurator(context).cameraAutoOff = value
        }

        /**
         * check if only audio call is dialed by default
         *
         * @param context the context
         * @return the boolean
         */
        fun getOnlyAudioCall(context: Context): Boolean =
            SharedPreferenceConfigurator(context).defaultAudioOnly

        /**
         * set only audio call to be dialed by default
         *
         * @param context the context
         * @param value the value
         */
        fun setOnlyAudioCall(context: Context, value: Boolean) {
            SharedPreferenceConfigurator(context).defaultAudioOnly = value
        }

    }
}