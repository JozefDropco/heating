package org.dropco.smarthome.watering2;

import org.dropco.smarthome.watering2.db.WateringRecord;

public class WateringScheduledWork implements Runnable {

    private WateringRecord wateringRecord;

    public WateringScheduledWork(WateringRecord wateringRecord) {
        this.wateringRecord = wateringRecord;
    }

    public void run() {
        WateringThreadManager.water(wateringRecord);
    }
}
