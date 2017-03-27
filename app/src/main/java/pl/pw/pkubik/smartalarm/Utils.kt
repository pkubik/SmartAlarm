package pl.pw.pkubik.smartalarm


import android.app.AlarmManager
import android.content.Context
import android.net.Uri
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*


object Utils : AnkoLogger {
    const val GOOGLE_API_KEY = BuildConfig.web_api_key
    const val GOOGLE_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

    fun msTimeToString(msTime: Long): String {
        if (msTime == 0L) {
            return "-"
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        return dateFormat.format(Date(msTime))
    }

    fun getNextAlarm(context: Context): AlarmManager.AlarmClockInfo? {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.nextAlarmClock
    }

    fun notifyOnNextAlarm(context: Context) {
        info("Created notification on the next alarm.")
    }

    fun buildTrafficUrl(origin: LatLng, destination: LatLng): URL? {
        val uri = Uri.parse(GOOGLE_BASE_URL).buildUpon()
                .appendQueryParameter("origin", "%f,%f".format(origin.latitude, origin.longitude))
                .appendQueryParameter("destination",
                        "%f,%f".format(destination.latitude, destination.longitude))
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
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connect()
                    if (connection.responseCode == 200) {
                        info("Received traffic response")
                        val json = Parser().parse(connection.inputStream) as JsonObject
                        val (time, trafficTime) = extractTimes(json)

                        uiThread {
                            responseListener.onSuccess(time, trafficTime)
                        }
                    } else {
                        error("Failed to query the traffic server")
                        uiThread { responseListener.onFailure() }
                    }
                } catch (e: Exception) {
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
}
