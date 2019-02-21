package org.dropco.smarthome.watering;

public class WateringRecord {
    private int month;
    private int day;
    private int hour;
    private int minute;
    private String zoneRefCode;
    private long timeInSeconds;
    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public String getZoneRefCode() {
        return zoneRefCode;
    }

    public void setZoneRefCode(String zoneRefCode) {
        this.zoneRefCode = zoneRefCode;
    }

    public long getTimeInSeconds() {
        return timeInSeconds;
    }

    public void setTimeInSeconds(long timeInSeconds) {
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WateringRecord{");
        sb.append("month=").append(month);
        sb.append(", day=").append(day);
        sb.append(", hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append(", zoneRefCode='").append(zoneRefCode).append('\'');
        sb.append(", timeInSeconds=").append(timeInSeconds);
        sb.append('}');
        return sb.toString();
    }
}
