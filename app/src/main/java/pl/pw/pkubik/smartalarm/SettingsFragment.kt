package pl.pw.pkubik.smartalarm

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat

import com.google.android.gms.location.places.Place
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class SettingsFragment : PreferenceFragmentCompat(), AnkoLogger {
    private val sharedPreferences by lazy { preferenceManager.sharedPreferences }
    private val checkpoints by lazy { arrayOf(Checkpoint(0, context), Checkpoint(1, context)) }
    val checkpointPrefs by lazy {
        arrayOf(findPreference("place_picker0"), findPreference("place_picker1")) }
    private val alarmPref by lazy { findPreference("next_alarm") }
    private val trafficPref by lazy { findPreference("current_traffic") }
    private var clickEnabled = true

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_alarm)
        val activity = activity as MainActivity?
        if (activity != null) {
            if (sharedPreferences != null) {
                for (checkpoint in checkpoints) {
                    checkpointPrefs[checkpoint.nr].summary = checkpoint.address
                    checkpointPrefs[checkpoint.nr].onPreferenceClickListener =
                            Preference.OnPreferenceClickListener {
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

            if (trafficPref != null) {
                trafficPref.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    if (clickEnabled) {
                        clickEnabled = false

                        Utils.checkTraffic(
                                activity,
                                checkpoints[0].latLng,
                                checkpoints[1].latLng,
                                object : Utils.TrafficResponseListener {
                                    override fun onSuccess(time: Int, trafficTime: Int) {
                                        clickEnabled = true
                                        trafficPref.summary =
                                                formatTrafficSummary(time, trafficTime)
                                    }

                                    override fun onFailure() {
                                        clickEnabled = true
                                    }
                                })
                        // TODO: remove
                        Utils.notifyAboutTraffic(context, 1.2345678f)
                    }
                    true
                }
            }
        }
    }

    fun formatTrafficSummary(time: Int, trafficTime: Int): String {
        if (time == Int.MAX_VALUE) {
            return ""
        } else {
            return "Usual time: %d min\nCurrent time: %d min".format(
                    time / 60,
                    trafficTime / 60)
        }
    }

    fun setPlace(nr: Int, place: Place) {
        info("setPlace: Setting place %d to %s".format(nr, place.name))
        val latLng = place.latLng
        val address = place.address.toString()

        checkpoints[nr].update(latLng.latitude, latLng.longitude, address)
        checkpointPrefs[nr].summary = address

        clickEnabled = true
    }

    fun setAlarmTime(time: Long) {
        val timeString = Utils.msTimeToString(time)
        info("setAlarmTime: Setting the affected alarm to " + timeString)
        alarmPref?.summary = timeString

        clickEnabled = true
    }
}
