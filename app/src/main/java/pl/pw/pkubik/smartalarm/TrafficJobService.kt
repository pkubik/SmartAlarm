package pl.pw.pkubik.smartalarm


import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log

class TrafficJobService : JobService() {

    override fun onStartJob(params: JobParameters): Boolean {
        Log.i(TAG, "onStartJob: Starting the traffic job.")
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        return false
    }

    companion object {
        private val TAG = TrafficJobService::class.java.name
    }
}
