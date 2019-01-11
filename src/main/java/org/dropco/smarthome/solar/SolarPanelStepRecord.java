package org.dropco.smarthome.solar;

public class SolarPanelStepRecord {
    private int month;
    private WeekDay weekDay;
    private int hour;
    private int minute;
    private SolarPanelPosition panelPosition;

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public WeekDay getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(WeekDay weekDay) {
        this.weekDay = weekDay;
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

    public SolarPanelPosition getPanelPosition() {
        return panelPosition;
    }

    public void setPanelPosition(SolarPanelPosition panelPosition) {
        this.panelPosition = panelPosition;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarPanelStepRecord{");
        sb.append("month=").append(month);
        sb.append(", weekDay=").append(weekDay);
        sb.append(", hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append(", panelPosition=").append(panelPosition);
        sb.append('}');
        return sb.toString();
    }
}
