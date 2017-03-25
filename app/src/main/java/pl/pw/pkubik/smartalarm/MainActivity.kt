package pl.pw.pkubik.smartalarm

import android.app.Activity
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.longToast

import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), AnkoLogger {
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsFragment =
                supportFragmentManager.findFragmentById(R.id.activity_settings) as SettingsFragment
        scheduleTrafficJob()
    }

    fun launchPlacePicker(id: Int, latitude: Double, longitude: Double) {
        info("Launching Place Picker intent.")
        val intentBuilder = PlacePicker.IntentBuilder()
        val bounds = LatLngBounds.builder()
                .include(LatLng(latitude, longitude))
                .build()
        intentBuilder.setLatLngBounds(bounds)
        try {
            startActivityForResult(intentBuilder.build(this), PLACE_PICKER_REQUEST + id)
        } catch (e: GooglePlayServicesRepairableException) {
            longToast("Can't connect Google Play Services.")
        } catch (e: GooglePlayServicesNotAvailableException) {
            longToast("Can't connect Google Play Services.")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode >= PLACE_PICKER_REQUEST) {
            info("onActivityResult: PLACE_PICKER_REQUEST")
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                settingsFragment.setPlace(requestCode - PLACE_PICKER_REQUEST, place)
            } else {
                info("onActivityResult: User did not pick a place.")
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun scheduleTrafficJob() {
        info("scheduleTrafficJob: Scheduling a job for the next alarm.")
        val nextAlarm = Utils.getNextAlarm(this)
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
        private val PLACE_PICKER_REQUEST = 10
        private val TRAFFIC_JOB_ID = 1
    }
}
