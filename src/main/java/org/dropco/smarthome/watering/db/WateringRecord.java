package org.dropco.smarthome.watering.db;

public class WateringRecord {
    private int reminder;
    private int modulo;
    private int hour;
    private int minute;
    private String zoneRefCode;
    private long timeInSeconds;
    private Integer retryHour;
    private Integer retryMinute;
    private boolean continuous;
    private boolean active;

    public int getReminder() {
        return reminder;
    }

    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    public int getModulo() {
        return modulo;
    }

    public void setModulo(int modulo) {
        this.modulo = modulo;
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

    public Integer getRetryHour() {
        return retryHour;
    }

    public void setRetryHour(Integer retryHour) {
        this.retryHour = retryHour;
    }

    public Integer getRetryMinute() {
        return retryMinute;
    }

    public void setRetryMinute(Integer retryMinute) {
        this.retryMinute = retryMinute;
    }

    public boolean isContinuous() {
        return continuous;
    }

    public void setContinuous(boolean continuous) {
        this.continuous = continuous;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WateringRecord{");
        sb.append("reminder=").append(reminder);
        sb.append(", modulo=").append(modulo);
        sb.append(", hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append(", zoneRefCode='").append(zoneRefCode).append('\'');
        sb.append(", timeInSeconds=").append(timeInSeconds);
        sb.append(", retryHour=").append(retryHour);
        sb.append(", retryMinute=").append(retryMinute);
        sb.append('}');
        return sb.toString();
    }

}
