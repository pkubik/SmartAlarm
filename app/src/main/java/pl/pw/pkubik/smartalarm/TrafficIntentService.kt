package pl.pw.pkubik.smartalarm

import android.app.IntentService
import android.content.Intent
import android.util.Log

class TrafficIntentService : IntentService(TrafficIntentService.SERVICE_NAME) {

    override fun onHandleIntent(intent: Intent?) {
        // TODO: Contact the Traffic API, etc.
        Log.i(TAG, "onHandleIntent: Checking the current traffic.")
    }

    companion object {
        private val TAG = TrafficIntentService::class.java.name
        private val SERVICE_NAME = TrafficIntentService::class.java.simpleName
    }
}
