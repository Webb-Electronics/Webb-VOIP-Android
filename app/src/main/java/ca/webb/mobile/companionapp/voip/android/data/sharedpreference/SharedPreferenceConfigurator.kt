package ca.webb.mobile.companionapp.voip.android.data.sharedpreference

import android.app.Activity
import android.content.Context

/**
 * Shared preference configurator
 *
 * @constructor Read and write to shared preference data for the app
 *
 * @param context the context of the activity
 */
class SharedPreferenceConfigurator(context: Context) {
    private val sharedPreference = context.getSharedPreferences(
        "ca.webb.mobile.companionapp.voip.android", Activity.MODE_PRIVATE
    )
    private val editor = sharedPreference.edit()
    var defaultAudioOnly: Boolean
        get() = sharedPreference.getBoolean("StartAudioOnlySession", false)
        set(value) {
            editor.putBoolean("StartAudioOnlySession", value)
            editor.apply()
        }

    var cameraAutoOff: Boolean
        get() = sharedPreference.getBoolean("CameraAutoOff", true)
        set(value) {
            editor.putBoolean("CameraAutoOff", value)
            editor.apply()
        }
}