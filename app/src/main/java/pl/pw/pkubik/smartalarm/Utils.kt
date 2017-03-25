package pl.pw.pkubik.smartalarm


import android.app.AlarmManager
import android.content.Context
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.info
import java.text.SimpleDateFormat
import java.util.*

object Utils : AnkoLogger {
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

    fun checkTraffic(context: Context, latitude: Float, longitute: Float): Unit {
        context.doAsync(Throwable::printStackTrace, {
            val result = 5
//            uiThread {
//                Log.i(TAG, "Received a result: " + result.toString())
//            }
        })
    }
}
