package org.dropco.smarthome.watering;

public class WateringScheduledWork implements Runnable {

    private String zone;
    private long timeInSeconds;

    public WateringScheduledWork(String zone, long timeInSeconds) {
        this.zone = zone;
        this.timeInSeconds = timeInSeconds;
    }

    public void run() {
        WateringThreadManager.move(zone, timeInSeconds);
    }
}
