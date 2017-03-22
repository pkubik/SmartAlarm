package pl.pw.pkubik.smartalarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int TRAFFIC_JOB_ID = 1;
    private SettingsFragment settingsFragment;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingsFragment = (SettingsFragment)
                getSupportFragmentManager().findFragmentById(R.id.activity_settings);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        scheduleTrafficJob();
    }

    public void launchPlacePicker(double latitude, double longitude) {
        Log.i(TAG, "Launching Place Picker intent.");
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        LatLngBounds bounds = LatLngBounds.builder()
                .include(new LatLng(latitude, longitude))
                .build();
        builder.setLatLngBounds(bounds);
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException |
                 GooglePlayServicesNotAvailableException e) {
            Toast.makeText(this, "Can't connect Google Play Services.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            Log.i(TAG, "onActivityResult: PLACE_PICKER_REQUEST");
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                settingsFragment.setPlace(place);
            } else {
                Log.i(TAG, "onActivityResult: User did not pick a place.");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void scheduleTrafficJob() {
        Log.i(TAG, "scheduleTrafficJob: Scheduling a job for the next alarm.");
        AlarmManager.AlarmClockInfo nextAlarm = alarmManager.getNextAlarmClock();
        if (nextAlarm != null) {
            long time = nextAlarm.getTriggerTime() - TimeUnit.MINUTES.toMillis(5);
            JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

            // Cancel the previous job
            scheduler.cancel(TRAFFIC_JOB_ID);

            long timeDiff = time - System.currentTimeMillis();
            JobInfo jobInfo = new JobInfo.Builder(
                    TRAFFIC_JOB_ID, new ComponentName(this, TrafficJobService.class))
                    .setMinimumLatency(timeDiff)
                    .build();
            scheduler.schedule(jobInfo);
            settingsFragment.setAlarmTime(nextAlarm.getTriggerTime());
        } else {
            Toast.makeText(this, "No alarm clock exists", Toast.LENGTH_LONG).show();
        }
    }
}
