package org.dropco.smarthome;


import java.util.Calendar;
import java.util.Date;

public class TimeUtil {


    public static boolean isAfter(Calendar calendar, int hour, int minute) {
        Date currentDate = calendar.getTime();
        Calendar after = Calendar.getInstance();
        after.setTime(currentDate);
        after.set(Calendar.HOUR_OF_DAY, hour);
        after.set(Calendar.MINUTE, minute);
        after.set(Calendar.SECOND, 0);
        after.set(Calendar.MILLISECOND, 0);
        return currentDate.compareTo(after.getTime()) != 1;
    }

    public static boolean isAfternoon(Calendar currentTime, String afternoonTime) {
        Calendar calculatedTime = Calendar.getInstance();
        calculatedTime.set(Calendar.SECOND, 0);
        calculatedTime.set(Calendar.MILLISECOND, 0);
        String[] split = afternoonTime.split(":");
        calculatedTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calculatedTime.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        return currentTime.after(calculatedTime);
    }

    public static long milisRemaingForNextDay() {
        return millisRemaining(Calendar.getInstance(), 23, 59) + 60 * 1000;
    }

    public static long millisRemaining(Calendar current, Date future) {
        Date currentDate = current.getTime();
        return future.getTime() - currentDate.getTime() + 3 * 1000;
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
