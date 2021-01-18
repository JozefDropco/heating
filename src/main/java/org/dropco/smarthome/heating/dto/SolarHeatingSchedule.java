package org.dropco.smarthome.heating.dto;

import java.sql.Time;
import java.time.LocalTime;

public class SolarHeatingSchedule {
    private int id;
    private int day;
    private LocalTime fromTime;
    private LocalTime toTime;
    private double threeWayValveStartDiff;
    private double threeWayValveStopDiff;
    private boolean boilerBlock;

    /***
     * Gets the id
     * @return
     */
    public int getId() {
        return id;
    }

    public SolarHeatingSchedule setId(int id) {
        this.id = id;
        return this;
    }

    /***
     * Gets the day
     * @return
     */
    public int getDay() {
        return day;
    }

    public SolarHeatingSchedule setDay(int day) {
        this.day = day;
        return this;
    }

    /***
     * Gets the fromTime
     * @return
     */
    public LocalTime getFromTime() {
        return fromTime;
    }

    public SolarHeatingSchedule setFromTime(LocalTime fromTime) {
        this.fromTime = fromTime;
        return this;
    }

    /***
     * Gets the toTime
     * @return
     */
    public LocalTime getToTime() {
        return toTime;
    }

    public SolarHeatingSchedule setToTime(LocalTime toTime) {
        this.toTime = toTime;
        return this;
    }

    /***
     * Gets the threeWayValveStartDiff
     * @return
     */
    public double getThreeWayValveStartDiff() {
        return threeWayValveStartDiff;
    }

    /***
     * Gets the threeWayValveStopDiff
     * @return
     */
    public double getThreeWayValveStopDiff() {
        return threeWayValveStopDiff;
    }

    public SolarHeatingSchedule setThreeWayValveStopDiff(double threeWayValveStopDiff) {
        this.threeWayValveStopDiff = threeWayValveStopDiff;
        return this;
    }

    public SolarHeatingSchedule setThreeWayValveStartDiff(double threeWayValveStartDiff) {
        this.threeWayValveStartDiff = threeWayValveStartDiff;
        return this;
    }

    /***
     * Gets the boilerBlock
     * @return
     */
    public boolean getBoilerBlock() {
        return boilerBlock;
    }

    public SolarHeatingSchedule setBoilerBlock(boolean boilerBlock) {
        this.boilerBlock = boilerBlock;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SolarHeatingSchedule{");
        sb.append("id=").append(id);
        sb.append(", day=").append(day);
        sb.append(", fromTime=").append(fromTime);
        sb.append(", toTime=").append(toTime);
        sb.append(", threeWayValveStartDiff=").append(threeWayValveStartDiff);
        sb.append(", threeWayValveStopDiff=").append(threeWayValveStopDiff);
        sb.append(", boilerBlock=").append(boilerBlock);
        sb.append('}');
        return sb.toString();
    }
}
