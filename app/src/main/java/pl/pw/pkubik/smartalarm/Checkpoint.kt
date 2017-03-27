package pl.pw.pkubik.smartalarm

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.defaultSharedPreferences

class Checkpoint(val nr: Int, context: Context) {
    val preferences = context.defaultSharedPreferences
    val latKey = context.getString(R.string.pref_latitude_key) + nr.toString()
    val lngKey = context.getString(R.string.pref_longitude_key) + nr.toString()
    val addressKey = context.getString(R.string.pref_address_key) + nr.toString()
    val lat: Double
        get() {
            return preferences.getFloat(latKey, 0.0f).toDouble()
        }
    val lng: Double
        get() {
            return preferences.getFloat(lngKey, 0.0f).toDouble()
        }
    val address: String
        get() {
            return preferences.getString(addressKey, "-")
        }

    fun update(lat: Double, lng: Double, address: String) {
        preferences?.edit()?.apply {
            putFloat(latKey, lat.toFloat())
            putFloat(lngKey, lng.toFloat())
            putString(addressKey, address)
            apply()
        }
    }

    val latLng: LatLng
        get() {
            return LatLng(lat, lng)
        }
}