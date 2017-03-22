package pl.pw.pkubik.smartalarm;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public class TrafficJobService extends JobService {
    private static final String TAG = TrafficJobService.class.getName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob: Starting the traffic job.");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
