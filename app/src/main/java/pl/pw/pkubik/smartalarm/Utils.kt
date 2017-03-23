package pl.pw.pkubik.smartalarm


import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

object Utils {
    fun msTimeToString(msTime: Long): String {
        if (msTime == 0L) {
            return "-"
        }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US)
        return dateFormat.format(Date(msTime))
    }
}

fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.longToast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
