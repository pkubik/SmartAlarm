package pl.pw.pkubik.smartalarm


import android.app.job.JobParameters
import android.app.job.JobService
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.info
import org.jetbrains.anko.warn
import org.jetbrains.anko.error


class TrafficJobService : JobService(), AnkoLogger {

    override fun onStartJob(params: JobParameters): Boolean {
        val checkpoints = arrayOf(Checkpoint(0, this), Checkpoint(1, this))
        val stringThreshold = defaultSharedPreferences.getString("delay_ratio", "200")
        val threshold = stringThreshold?.toFloat()?.div(100)

        Utils.checkTraffic(
                this,
                checkpoints[0].latLng,
                checkpoints[1].latLng,
                object : Utils.TrafficResponseListener {
                    override fun onSuccess(time: Int, trafficTime: Int) {
                        val ratio = trafficTime.toFloat() / time.toFloat()
                        Utils.notifyAboutTraffic(this@TrafficJobService, ratio)

                        if (threshold != null) {
                            if (ratio > threshold &&
                                    defaultSharedPreferences.getBoolean("auto_dismiss", false)) {
                                info("Threshold met - suppressing the alarm.")
                            } else {
                                // All fine
                                Utils.runAlarm(this@TrafficJobService)
                            }
                        } else {
                            warn("Threshold not set - launching the alarm.")
                            Utils.runAlarm(this@TrafficJobService)
                        }

                        jobFinished(params, false)
                    }

                    override fun onFailure() {
                        error("Failed to retrieve traffic info.")
                        Utils.runAlarm(this@TrafficJobService)
                        jobFinished(params, false)
                    }
                })
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        info("onStartJob: Stopping the traffic job.")
        return false
    }
}
