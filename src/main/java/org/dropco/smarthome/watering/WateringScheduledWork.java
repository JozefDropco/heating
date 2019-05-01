package org.dropco.smarthome.watering;

import org.dropco.smarthome.watering.db.WateringRecord;

public class WateringScheduledWork implements Runnable {

    private WateringRecord wateringRecord;

    public WateringScheduledWork(WateringRecord wateringRecord) {
        this.wateringRecord = wateringRecord;
    }

    public void run() {
        WateringThreadManager.water(wateringRecord);
    }
}
