package pl.pw.pkubik.smartalarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.longToast


class MainActivity : Activity(), AnkoLogger {
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settingsFragment =
                fragmentManager.findFragmentById(R.id.activity_settings) as SettingsFragment
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onResume() {
        isRunning = true
        super.onResume()
    }

    override fun onPause() {
        isRunning = false
        super.onDestroy()
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

    fun scheduleTrafficJob(time: Long?) {
        info("scheduleTrafficJob: Scheduling a job for the next alarm.")
        val nextAlarm = Utils.getNextAlarm(this)
        if (nextAlarm != null) {
            longToast("Another alarm clock is already scheduled!")
        }

        if (time != null) {
            Utils.scheduleTrafficJob(this, time)
        } else {
            error("Received null as the time")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        if (id == R.id.preview_option) {
            Utils.runAlarm(this)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private val PLACE_PICKER_REQUEST = 10
        var isRunning = false
    }
}
