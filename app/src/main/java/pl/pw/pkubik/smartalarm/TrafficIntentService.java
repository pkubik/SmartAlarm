package pl.pw.pkubik.smartalarm;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

public class TrafficIntentService extends IntentService {
    private static final String TAG = TrafficIntentService.class.getName();
    private static final String SERVICE_NAME = TrafficIntentService.class.getSimpleName();

    public TrafficIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO: Contact the Traffic API, etc.
        Log.i(TAG, "onHandleIntent: Checking the current traffic.");
    }
}
