package pl.pw.pkubik.smartalarm

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log

import com.google.android.gms.location.places.Place

class SettingsFragment : PreferenceFragmentCompat() {
    private val latitudeKey by lazy { getString(R.string.pref_latitude_key) }
    private val longitudeKey by lazy { getString(R.string.pref_longitude_key) }
    private val addressKey by lazy { getString(R.string.pref_address_key) }
    private val placePref by lazy { findPreference("place_picker") }
    private val alarmPref by lazy { findPreference("next_alarm") }
    private var clickEnabled = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_alarm)
        val sharedPreferences = preferenceManager.sharedPreferences
        val activity = activity as MainActivity?
        if (activity != null) {
            if (sharedPreferences != null && placePref != null) {
                placePref.summary = sharedPreferences.getString(addressKey, "")
                placePref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    if (clickEnabled) {
                        clickEnabled = false

                        val latitude = sharedPreferences.getFloat(latitudeKey, 0.0f).toDouble()
                        val longitude = sharedPreferences.getFloat(longitudeKey, 0.0f).toDouble()

                        activity.launchPlacePicker(latitude, longitude)
                    }
                    true
                }
            }

            if (alarmPref != null) {
                alarmPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    if (clickEnabled) {
                        clickEnabled = false

                        activity.scheduleTrafficJob()
                    }
                    true
                }
            }
        }
    }

    fun setPlace(place: Place) {
        Log.i(TAG, "setPlace: Setting the place to " + place.name)
        val latLng = place.latLng
        val address = place.address.toString()

        val sharedPreferences = preferenceManager.sharedPreferences

        sharedPreferences?.edit()?.apply {
            putFloat(latitudeKey, latLng.latitude.toFloat())
            putFloat(longitudeKey, latLng.longitude.toFloat())
            putString(addressKey, address)

            apply()
        }

        placePref.summary = address

        clickEnabled = true
    }

    fun setAlarmTime(time: Long) {
        val timeString = Utils.msTimeToString(time)
        Log.i(TAG, "setAlarmTime: Setting the affected alarm to " + timeString)
        alarmPref?.summary = timeString

        clickEnabled = true
    }

    companion object {
        private val TAG = SettingsFragment::class.java.name
    }
}
