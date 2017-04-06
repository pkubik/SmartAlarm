package pl.pw.pkubik.smartalarm

import android.content.res.TypedArray
import android.widget.TimePicker
import android.content.Context
import android.preference.DialogPreference
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import java.util.*
import java.util.concurrent.TimeUnit


class TimePreference @JvmOverloads constructor(
        context: Context?,
        attrs: AttributeSet? = null,
        defStyle: Int = android.R.attr.dialogPreferenceStyle)
        : DialogPreference(context, attrs, defStyle) {
    private val calendar: Calendar = GregorianCalendar()
    private var picker: TimePicker? = null

    init {
        setPositiveButtonText(android.R.string.ok)
        setNegativeButtonText(android.R.string.cancel)
    }

    override fun onCreateDialogView(): View? {
        picker = TimePicker(context)
        return picker
    }

    override fun onBindDialogView(view: View?) {
        super.onBindDialogView(view)
        picker?.hour = calendar.get(Calendar.HOUR_OF_DAY)
        picker?.minute = calendar.get(Calendar.MINUTE)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)

        if (positiveResult) {
            val currentTime = System.currentTimeMillis()
            calendar.timeInMillis = currentTime
            calendar.set(Calendar.HOUR_OF_DAY, picker!!.hour)
            calendar.set(Calendar.MINUTE, picker!!.minute)

            if (calendar.timeInMillis < currentTime) {
                calendar.timeInMillis += TimeUnit.DAYS.toMillis(1)
            }

            setSummary(summary)

            if (callChangeListener(calendar.timeInMillis)) {
                persistLong(calendar.timeInMillis)
                notifyChanged()
            }
        }
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getString(index)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {

        val defaultValue = defaultValue as? String
        if (restoreValue) {
            if (defaultValue == null) {
                calendar.timeInMillis = getPersistedLong(System.currentTimeMillis())
            } else {
                calendar.timeInMillis = java.lang.Long.parseLong(getPersistedString(defaultValue))
            }
        } else {
            if (defaultValue == null) {
                calendar.timeInMillis = System.currentTimeMillis()
            } else {
                calendar.timeInMillis = java.lang.Long.parseLong(defaultValue)
            }
        }
        setSummary(summary)
    }

    override fun getSummary(): CharSequence? {
        return DateFormat.getTimeFormat(context).format(Date(calendar.timeInMillis))
    }
}