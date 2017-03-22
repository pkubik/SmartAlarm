package pl.pw.pkubik.smartalarm;


import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    static public String msTimeToString(Long msTime) {
        if (msTime == 0) {
            return "-";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return dateFormat.format(new Date(msTime));
    }
}
