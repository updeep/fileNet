package application.server.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerTimeUtil
{
    public static String accurateToSecond() {
        final Date d = new Date();
        final DateFormat df = new SimpleDateFormat("yyyy\u5e74MM\u6708dd\u65e5 HH:mm:ss");
        return df.format(d);
    }
    
    public static String accurateToMinute() {
        final Date d = new Date();
        final DateFormat df = new SimpleDateFormat("yyyy\u5e74MM\u6708dd\u65e5 HH:mm");
        return df.format(d);
    }
    
    public static String accurateToDay() {
        final Date d = new Date();
        final DateFormat df = new SimpleDateFormat("yyyy\u5e74MM\u6708dd\u65e5");
        return df.format(d);
    }
    
    public static String accurateToLogName() {
        final Date d = new Date();
        final DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        return df.format(d);
    }
    
    public static Date getServerTime() {
        return new Date();
    }
}
