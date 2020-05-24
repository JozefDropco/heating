package org.dropco.smarthome.watering;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.temp.TempService;
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
                    Thread thread = new Thread(new WateringJob(wateringRecord));
                    stop(thread);
                    thread.start();
            } else {
                LOGGER.log(Level.INFO, "Teplota je nižšia ako "+thresholdTempValue+" stupňov.");
                tryReschedule(wateringRecord, 0);
            }
        } else {
            LOGGER.log(Level.INFO, "Servisný mód zapnutý, polievanie zastavené.");
            tryReschedule(wateringRecord, 0);
        }

    }

    public static void tryReschedule(WateringRecord record, int elapsedSeconds) {
        if (record.getRetryHour() != null && record.getRetryMinute() != null) {
            record.setHour(record.getRetryHour());
            record.setMinute(record.getRetryMinute());
            record.setRetryHour(null);
            record.setRetryMinute(null);
            record.setTimeInSeconds(record.getTimeInSeconds() - elapsedSeconds);
            LOGGER.log(Level.INFO, "Polievanie prerušené, pokus o znovu polievanie. ");
            WateringScheduler.schedule(record);
        }
    }

    public static void stop() {
        stop(null);
    }

    private static void stop(Thread thread) {
        Thread oldThread = lastThread.getAndSet(thread);
        if (oldThread != null) {
            LOGGER.log(Level.INFO, "Zastavujem polievanie");
            oldThread.interrupt();

            try {
                LOGGER.log(Level.INFO, "Čakám na ukončenie polievania");
                oldThread.join();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Čakanie na ukončenie polievania prerušené");
            }
        }
    }

    public static Thread getCurrent() {
        return lastThread.get();
    }

    private static boolean isWarmEnough() {
        double temperature = TempService.getOutsideTemperature();
        if (temperature==-999.0) return true;
        return thresholdTempValue< temperature;
    }

}
