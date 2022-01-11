package org.dropco.smarthome;


import java.util.Calendar;
import java.util.Date;

public class TimeUtil {


    public static boolean isAfter(Calendar calendar, int hour, int minute) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour < hour) return true;
        if (currentHour == hour && calendar.get(Calendar.MINUTE) <= minute) return true;
        return false;
    }

    public static boolean isAfternoon(Calendar currentTime, String afternoonTime) {
        Calendar calculatedTime = Calendar.getInstance();
        calculatedTime.set(Calendar.SECOND, 0);
        calculatedTime.set(Calendar.MILLISECOND, 0);
        String[] split = afternoonTime.split(":");
        calculatedTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calculatedTime.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        return calculatedTime.after(currentTime);
    }

    public static long milisRemaingForNextDay() {
        return millisRemaining(Calendar.getInstance(), 23, 59) + 60 * 1000;
    }

    public static long millisRemaining(Calendar current, int hour, int minute) {
        Date currentDate = current.getTime();
        current.set(Calendar.HOUR_OF_DAY, hour);
        current.set(Calendar.MINUTE, minute);
        current.set(Calendar.SECOND, 5);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        return future.getTime() - currentDate.getTime();
    }

    public static boolean isToday(Date asOfDate) {
        Calendar instance = Calendar.getInstance();
        Calendar asOf = Calendar.getInstance();
        asOf.setTime(asOfDate);
        if (instance.get(Calendar.YEAR) != asOf.get(Calendar.YEAR)) return false;
        if (instance.get(Calendar.DAY_OF_YEAR) != asOf.get(Calendar.DAY_OF_YEAR)) return false;
        return true;
    }
}
