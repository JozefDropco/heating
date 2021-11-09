package org.dropco.smarthome.heating.solar.dto;

public class SolarPanelStep {
    private int hour;
    private int minute;
    private Position position;
    private boolean ignoreDayLight;

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

    /***
     * Gets the ignoreDayLight
     * @return
     */
    public boolean getIgnoreDayLight() {
        return ignoreDayLight;
    }

    public void setIgnoreDayLight(boolean ignoreDayLight) {
        this.ignoreDayLight = ignoreDayLight;
    }

    /***
     * Gets the position
     * @return
     */
    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarPanelStepRecord{");
        sb.append("hour=").append(hour);
        sb.append(", minute=").append(minute);
        sb.append(", position=").append(position);
        sb.append(", ignoreDayLight=").append(ignoreDayLight);
        sb.append('}');
        return sb.toString();
    }
}
