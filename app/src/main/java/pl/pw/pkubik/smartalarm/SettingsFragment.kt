package pl.pw.pkubik.smartalarm

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

import com.google.android.gms.location.places.Place
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class SettingsFragment : PreferenceFragmentCompat(), AnkoLogger {
    inner class Checkpoint(val nr: Int) {
        val mainKey = "place_picker" + nr.toString()
        val latKey = getString(R.string.pref_latitude_key) + nr.toString()
        val lngKey = getString(R.string.pref_longitude_key) + nr.toString()
        val addressKey = getString(R.string.pref_address_key) + nr.toString()
        val pref: Preference = findPreference(mainKey)
        val lat: Double
            get() {
                return sharedPreferences.getFloat(latKey, 0.0f).toDouble()
            }
        val lng: Double
            get() {
                return sharedPreferences.getFloat(lngKey, 0.0f).toDouble()
            }
        val address: String
            get() {
                return sharedPreferences.getString(addressKey, "-")
            }

        fun update(lat: Double, lng: Double, address: String) {
            sharedPreferences?.edit()?.apply {
                putFloat(latKey, lat.toFloat())
                putFloat(lngKey, lng.toFloat())
                putString(addressKey, address)

                apply()
            }
        }
    }

    private val sharedPreferences by lazy { preferenceManager.sharedPreferences }
    private val checkpoints by lazy { arrayOf(Checkpoint(0), Checkpoint(1)) }
    private val alarmPref by lazy { findPreference("next_alarm") }
    private var clickEnabled = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_alarm)
        val activity = activity as MainActivity?
        if (activity != null) {
            if (sharedPreferences != null) {
                for (checkpoint in checkpoints) {
                    checkpoint.pref.summary = checkpoint.address
                    checkpoint.pref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                        if (clickEnabled) {
                            clickEnabled = false

                            activity.launchPlacePicker(
                                    checkpoint.nr,
                                    checkpoint.lat,
                                    checkpoint.lng)
                        }
                        true
                    }
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

    fun setPlace(nr: Int, place: Place) {
        info("setPlace: Setting place %d to %s".format(nr, place.name))
        val latLng = place.latLng
        val address = place.address.toString()

        checkpoints[nr].apply {
            update(latLng.latitude, latLng.longitude, address)
            pref.summary = address
        }

        clickEnabled = true
    }

    fun setAlarmTime(time: Long) {
        val timeString = Utils.msTimeToString(time)
        info("setAlarmTime: Setting the affected alarm to " + timeString)
        alarmPref?.summary = timeString

        clickEnabled = true
    }
}
