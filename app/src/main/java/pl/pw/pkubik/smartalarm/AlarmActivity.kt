package pl.pw.pkubik.smartalarm

import android.app.Activity
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class AlarmActivity : Activity() {
    private var mContentView: View? = null
    private lateinit var mRingtone: Ringtone
    private var mControlsView: View? = null

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val dismissButtonListener = View.OnTouchListener { view, motionEvent ->
        mRingtone.stop()
        finish()
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_alarm)

        mControlsView = findViewById(R.id.fullscreen_content_controls)
        mContentView = findViewById(R.id.fullscreen_content)

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dismiss_button).setOnTouchListener(dismissButtonListener)

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        mRingtone = RingtoneManager.getRingtone(this, notification)
        if (!mRingtone.isPlaying) {
            mRingtone.play()
        }
    }

    override fun onDestroy() {
        if (mRingtone.isPlaying) {
            mRingtone.stop()
        }
        super.onDestroy()
    }
}
