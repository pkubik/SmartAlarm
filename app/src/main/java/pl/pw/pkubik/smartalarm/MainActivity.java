package pl.pw.pkubik.smartalarm;

import android.app.Activity;
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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        settingsFragment = (SettingsFragment)
                getSupportFragmentManager().findFragmentById(R.id.activity_settings);
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
}
