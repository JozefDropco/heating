package org.dropco.smarthome.watering;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.microservice.OutsideTemperature;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.watering.db.WateringRecord;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WateringThreadManager {
    private static final Logger LOGGER = Logger.getLogger(WateringThreadManager.class.getName());
    private static final AtomicReference<Thread> lastThread = new AtomicReference<>();
    public static double thresholdTempValue =  5;

    static void water(WateringRecord wateringRecord) {
        stop();
        if (!ServiceMode.isServiceMode()) {
            if (isWarmEnough()) {
                if (WaterPumpFeedback.getRunning()) {
                    Thread thread = new Thread(new WateringJob(wateringRecord));
                    stop(thread);
                    LOGGER.log(Level.INFO, "Starting new thread with id=" + thread.getId() + " which will start to water zone=" + wateringRecord.getZoneRefCode() + ", seconds=" + wateringRecord.getTimeInSeconds());
                    thread.start();
                } else {
                    LOGGER.log(Level.INFO, "Pump is not ok");
                    tryReschedule(wateringRecord);
                }
            } else {
                LOGGER.log(Level.INFO, "Outside is not warm enough");
                tryReschedule(wateringRecord);
            }
        } else {
            LOGGER.log(Level.INFO, "Service mode started, ignoring watering");
            tryReschedule(wateringRecord);
        }

    }

    private static void tryReschedule(WateringRecord record) {
        if (record.getRetryHour() != null && record.getRetryMinute() != null) {
            record.setHour(record.getRetryHour());
            record.setMinute(record.getRetryMinute());
            record.setRetryHour(null);
            record.setRetryMinute(null);
            LOGGER.log(Level.INFO, "Retry mechanism triggerd. Rescheduled for "+record);
            WateringScheduler.schedule(record);
        }
    }

    public static void stop() {
        stop(null);
    }

    private static void stop(Thread thread) {
        Thread oldThread = lastThread.getAndSet(thread);
        if (oldThread != null) {
            LOGGER.log(Level.INFO, "Stopping the current thread - " + oldThread.getId());
            oldThread.interrupt();

            try {
                LOGGER.log(Level.INFO, "Waiting for current thread - " + oldThread.getId() + " to stop.");
                oldThread.join();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Waiting for current " + oldThread.getId() + " was interrupted.");
            }
        }
    }

    public static Thread getCurrent() {
        return lastThread.get();
    }

    private static boolean isWarmEnough() {
        double temperature = OutsideTemperature.getTemperature();
        if (temperature==-999.0) return true;
        return thresholdTempValue< temperature;
    }

}
