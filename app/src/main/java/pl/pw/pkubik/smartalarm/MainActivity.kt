package pl.pw.pkubik.smartalarm

import android.app.Activity
import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsFragment =
                supportFragmentManager.findFragmentById(R.id.activity_settings) as SettingsFragment
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        scheduleTrafficJob()
    }

    fun launchPlacePicker(latitude: Double, longitude: Double) {
        Log.i(TAG, "Launching Place Picker intent.")
        val intentBuilder = PlacePicker.IntentBuilder()
        val bounds = LatLngBounds.builder()
                .include(LatLng(latitude, longitude))
                .build()
        intentBuilder.setLatLngBounds(bounds)
        try {
            startActivityForResult(intentBuilder.build(this), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            longToast("Can't connect Google Play Services.")
        } catch (e: GooglePlayServicesNotAvailableException) {
            longToast("Can't connect Google Play Services.")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            Log.i(TAG, "onActivityResult: PLACE_PICKER_REQUEST")
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                settingsFragment.setPlace(place)
            } else {
                Log.i(TAG, "onActivityResult: User did not pick a place.")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun scheduleTrafficJob() {
        Log.i(TAG, "scheduleTrafficJob: Scheduling a job for the next alarm.")
        val nextAlarm = alarmManager.nextAlarmClock
        if (nextAlarm != null) {
            val time = nextAlarm.triggerTime - TimeUnit.MINUTES.toMillis(5)
            val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            // Cancel the previous job
            scheduler.cancel(TRAFFIC_JOB_ID)

            val timeDiff = time - System.currentTimeMillis()
            val jobInfo = JobInfo.Builder(
                    TRAFFIC_JOB_ID, ComponentName(this, TrafficJobService::class.java))
                    .setMinimumLatency(timeDiff)
                    .build()
            scheduler.schedule(jobInfo)
            settingsFragment.setAlarmTime(nextAlarm.triggerTime)
        } else {
            longToast("No alarm clock exists")
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.name
        private val PLACE_PICKER_REQUEST = 1
        private val TRAFFIC_JOB_ID = 1
    }
}
