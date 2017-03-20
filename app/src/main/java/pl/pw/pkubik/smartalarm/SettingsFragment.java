package pl.pw.pkubik.smartalarm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static final String TAG = SettingsFragment.class.getName();
    private String latitudeKey;
    private String longitudeKey;
    private String addressKey;
    private MainActivity activity;
    private SharedPreferences sharedPreferences;
    private Preference placePref;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_alarm);
        latitudeKey = getString(R.string.pref_latitude_key);
        longitudeKey = getString(R.string.pref_longitude_key);
        addressKey = getString(R.string.pref_address_key);
        activity = (MainActivity) getActivity();
        sharedPreferences = getPreferenceManager().getSharedPreferences();

        placePref = findPreference("place_picker");
        placePref.setSummary(sharedPreferences.getString(addressKey, ""));
        placePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                double latitude = sharedPreferences.getFloat(latitudeKey, 0.0f);
                double longitude = sharedPreferences.getFloat(longitudeKey, 0.0f);

                // Pass some id to this function to discriminate different preferences
                // handled by the same activity
                activity.launchPlacePicker(latitude, longitude);

                return true;
            }
        });
    }

    public void setPlace(Place place) {
        Log.i(TAG, "setPlace: Setting the place to " + place.getName());
        LatLng latLng = place.getLatLng();
        String address = place.getAddress().toString();

        SharedPreferences.Editor prefEditor = sharedPreferences.edit();

        prefEditor.putFloat(latitudeKey, (float) latLng.latitude);
        prefEditor.putFloat(longitudeKey, (float) latLng.longitude);
        prefEditor.putString(addressKey, address);

        prefEditor.apply();

        placePref.setSummary(address);
    }
}
