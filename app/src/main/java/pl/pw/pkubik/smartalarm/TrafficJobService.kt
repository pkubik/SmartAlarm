package pl.pw.pkubik.smartalarm


import android.app.job.JobParameters
import android.app.job.JobService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.info


class TrafficJobService : JobService(), AnkoLogger {

    override fun onStartJob(params: JobParameters): Boolean {
        val preferences = defaultSharedPreferences
//        val lat0 = preferences.getFloat("latitude0", 0.0f)
//        val lng0 = preferences.getFloat("longitude0", 0.0f)
//        val lat0 = preferences.getFloat("latitude0", 0.0f)
//        val lng0 = preferences.getFloat("longitude0", 0.0f)
//        info("onStartJob: Starting the traffic job for location (%f, %f)"
//                .format(latitude, longitude))
//        Utils.checkTraffic(this, latitude, longitude)
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        info("onStartJob: Stopping the traffic job.")
        return false
    }
}
