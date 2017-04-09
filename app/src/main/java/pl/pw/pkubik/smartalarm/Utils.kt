package pl.pw.pkubik.smartalarm


import android.app.AlarmManager
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.support.v7.app.NotificationCompat
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.*
import java.net.MalformedURLException
import java.net.URL
import android.content.Context.NOTIFICATION_SERVICE
import java.util.*
import javax.net.ssl.HttpsURLConnection
import android.content.Intent
import java.util.concurrent.TimeUnit


object Utils : AnkoLogger {
    const val GOOGLE_API_KEY = BuildConfig.web_api_key
    const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"
    const val TRAFFIC_JOB_ID = 1

    fun getNextAlarm(context: Context): AlarmManager.AlarmClockInfo? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.nextAlarmClock
    }

    fun notifyAboutTraffic(context: Context, ratio: Float) {
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.perm_group_system_clock)
                .setContentTitle("Traffic problems detected!")
                .setContentText("Delay ratio: %.2f".format(ratio))

        val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

        info("Created notification about traffic.")
    }

    fun buildTrafficUrl(origin: LatLng, destination: LatLng): URL? {
        val uri = Uri.parse(GOOGLE_BASE_URL).buildUpon()
                .appendQueryParameter("origin",
                        "%.10f,%.10f".format(Locale.US, origin.latitude, origin.longitude))
                .appendQueryParameter("destination",
                        "%.10f,%.10f".format(Locale.US, destination.latitude, destination.longitude))
                .appendQueryParameter("key", GOOGLE_API_KEY)
                .appendQueryParameter("departure_time", "now")
                .appendQueryParameter("traffic_model", "best_guess")
                .build()

        val url: URL? = try {
            URL(uri.toString())
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        }

        return url
    }

    interface TrafficResponseListener {
        fun onSuccess(time: Int, trafficTime: Int): Unit
        fun onFailure(): Unit
    }

    fun checkTraffic(context: Context, start: LatLng, end: LatLng,
                     responseListener: TrafficResponseListener) {
        info("Checking the traffic between " + start.toString() + " and "+ end.toString())
        context.doAsync(Throwable::printStackTrace, {
            val url = buildTrafficUrl(start, end)
            if (url == null) {
                error("Failed to build a query")
                uiThread { responseListener.onFailure() }
            } else {
                try {
                    val connection = url.openConnection() as HttpsURLConnection
                    connection.connect()
                    if (connection.responseCode == 200) {
                        info("Received traffic response")
                        val json = Parser().parse(connection.inputStream) as JsonObject
                        val (time, trafficTime) = extractTimes(json)

                        if (time == Int.MAX_VALUE) {
                            info("Route not found")
                            uiThread { responseListener.onFailure() }
                        } else {
                            uiThread {
                                responseListener.onSuccess(time, trafficTime)
                            }
                        }
                    } else {
                        error("Failed to query the traffic server")
                        uiThread { responseListener.onFailure() }
                    }
                } catch (e: Exception) {
                    error("Failed to query the traffic server")
                    e.printStackTrace()
                    uiThread { responseListener.onFailure() }
                }
            }
        })
    }

    private fun extractTimes(json: JsonObject): Pair<Int, Int> {
        fun mapValue(obj: JsonObject?, fn: (Int) -> Unit) {
            if (obj != null) {
                val value = obj["value"] as? Int
                if (value != null) {
                    fn(value)
                }
            }
        }

        var time = Int.MAX_VALUE
        var trafficTime = Int.MAX_VALUE
        for (route in json.getOrDefault("routes", JsonArray<JsonObject>()) as JsonArray<*>) {
            val r = route as JsonObject
            for (leg in r.getOrDefault("legs", JsonArray<JsonObject>()) as JsonArray<*>) {
                val l = leg as JsonObject
                mapValue(l["duration"] as? JsonObject, {
                    time = minOf(it, time)
                })
                mapValue(l["duration_in_traffic"] as? JsonObject, {
                    trafficTime = minOf(it, trafficTime)
                })
            }
        }

        return Pair(time, trafficTime)
    }

    fun scheduleTrafficJob(context: Context, time: Long) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        // Cancel the previous job
        scheduler.cancel(TRAFFIC_JOB_ID)

        val timeDiff = time - System.currentTimeMillis()
        if (timeDiff > TimeUnit.SECONDS.toMillis(30)) {
            val jobInfo = JobInfo.Builder(
                    TRAFFIC_JOB_ID, ComponentName(context, TrafficJobService::class.java))
                    .setMinimumLatency(timeDiff)
                    .build()
            scheduler.schedule(jobInfo)
        }
    }

    fun cancelTrafficJob(context: Context) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(TRAFFIC_JOB_ID)
    }

    fun runAlarm(context: Context) {
        info("Running the alarm")
        val intent = Intent(context, AlarmActivity::class.java)
        if (!MainActivity.isRunning)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
