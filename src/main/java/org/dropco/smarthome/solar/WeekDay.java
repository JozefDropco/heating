package org.dropco.smarthome.solar;

import java.util.Calendar;

public enum WeekDay {
    MONDAY(Calendar.MONDAY),
    TUESDAY(Calendar.TUESDAY),
    WEDNESDAY(Calendar.WEDNESDAY),
    THURSDAY(Calendar.THURSDAY),
    FRIDAY(Calendar.FRIDAY),
    SATURDAY(Calendar.SATURDAY),
    SUNDAY(Calendar.SUNDAY);

    int calendarDay;

    WeekDay(int calendarDay) {
        this.calendarDay = calendarDay;
    }

    public int getCalendarDay() {
        return calendarDay;
    }

    public static WeekDay fromCalendarDay(int day) {
        for (WeekDay weekDay : values()) {
            if (weekDay.calendarDay == day) return weekDay;
        }
        return null;
    }
}
