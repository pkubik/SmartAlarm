package pl.pw.pkubik.smartalarm

import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.Preference

import com.google.android.gms.location.places.Place
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class SettingsFragment : PreferenceFragment(), AnkoLogger {
    private val sharedPreferences by lazy { preferenceManager.sharedPreferences }
    private val checkpoints by lazy { arrayOf(Checkpoint(0, context), Checkpoint(1, context)) }
    val checkpointPrefs by lazy {
        arrayOf(findPreference("place_picker0"), findPreference("place_picker1")) }
    private val timePref by lazy { findPreference("alarm_time") }
    private val trafficPref by lazy { findPreference("current_traffic") }
    private var clickEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            if (timePref != null) {
                timePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                    _, newValue -> activity.scheduleTrafficJob(newValue as? Long); true
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
                                        trafficPref.summary = "(network error)"
                                    }
                                })
                    }
                    true
                }
            }

            val enableAllPref = findPreference("enable_all")
            if (enableAllPref != null) {
                enableAllPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                    _, newValue -> when (newValue) {
                    false -> { Utils.cancelTrafficJob(activity); true }
                    else -> {
                        val time = sharedPreferences.getLong("alarm_time", 0)
                        Utils.scheduleTrafficJob(activity, time)
                        true
                    }
                }
                }
            }
        }
    }

    fun formatTrafficSummary(time: Int, trafficTime: Int): String {
        if (time == Int.MAX_VALUE) {
            return trafficPref.summary.toString()
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
}
