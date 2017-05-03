package edu.sfsu.csc780.chathub;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by rajatar08 on 5/3/17.
 */

public class DateUtil {

    public static Date getUTCTime() {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date returnDate = new Date(sdf.format(date));
        return returnDate;
    }

    public static String toLocalTime(Date date) {

        String timeZone = Calendar.getInstance().getTimeZone().getID();
        Date local = new Date(date.getTime() + TimeZone.getTimeZone(timeZone).getOffset(date.getTime()));
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, MMM d");
        return sdf.format(local);
    }

}
