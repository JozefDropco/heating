package org.dropco.smarthome.watering;

import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.watering.db.WateringRecord;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.FluentIterable.from;

public class WateringJob implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(WateringJob.class.getName());

    private static final Level LEVEL = Level.INFO;

    private static Supplier<Set<String>> zones;
    private static BiConsumer<String, Boolean> commandExecutor;
    private Thread thisThread;
    private WateringRecord record;
    private Consumer<Boolean> rainSubscriber = isRaining -> {
        set(record.getZoneRefCode(), !isRaining);
        if (!isRaining) {
            try {
                sleep(3);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Sleep interrupted");
            }
            if (!WaterPumpFeedback.getRunning()) {
                LOGGER.log(Level.INFO, "Pump is not running, stop watering the zone " + record);
                thisThread.interrupt();
            }
        }
    };
    private Consumer<Boolean> pumpSubscriber = running -> {
        if (!running) {
            LOGGER.log(Level.INFO, "Pump is not running, stop watering the zone " + record);
            thisThread.interrupt();
        }
    };

    WateringJob(WateringRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        thisThread = Thread.currentThread();
        long before = System.currentTimeMillis();
        try {
            RainSensor.subscribe(rainSubscriber);

            set(record.getZoneRefCode(), !RainSensor.isRaining());
            closeOtherZones(record.getZoneRefCode());
            sleep(3);
            if (!WaterPumpFeedback.getRunning()) {
                LOGGER.log(Level.INFO, "Pump is not running, stop watering the zone " + record);
                thisThread.interrupt();
            }
            WaterPumpFeedback.subscribe(pumpSubscriber);
            sleep(record.getTimeInSeconds() - 3);
            set(record.getZoneRefCode(), false);
        } catch (InterruptedException ex) {
            if (!WaterPumpFeedback.getRunning()) {
                int elapsedSeconds = Math.abs((int) ((System.currentTimeMillis() - before) / 1000));
                LOGGER.log(Level.INFO, "Sleep interrupted. Retry mechanism in action");
                if (record.getRetryHour() != null && record.getRetryMinute() != null) {
                    record.setHour(record.getRetryHour());
                    record.setMinute(record.getRetryMinute());
                    record.setRetryHour(null);
                    record.setRetryMinute(null);
                    record.setTimeInSeconds(record.getTimeInSeconds() - elapsedSeconds);
                    WateringScheduler.schedule(record);
                }
                set(record.getZoneRefCode(), false);
            }
        } finally {
            RainSensor.unsubscribe(rainSubscriber);
            WaterPumpFeedback.unsubscribe(pumpSubscriber);
        }

    }


    void sleep(long timeInSeconds) throws InterruptedException {
        LOGGER.log(Level.INFO, "Sleeping for next " + timeInSeconds + " seconds.");
        Thread.sleep(timeInSeconds * 1000);
    }

    void set(String zoneRefCode, boolean state) {
        commandExecutor.accept(zoneRefCode, state);
        LOGGER.log(LEVEL, "Zone with REF_CD= " + record.getZoneRefCode() + (state ? " opened." : " closed."));

    }

    void closeOtherZones(String zoneRefCode) {
        Set<String> allZones = zones.get();
        from(allZones).filter(zone -> !zone.equals(zoneRefCode)).forEach(z -> commandExecutor.accept(z, false));
        LOGGER.log(LEVEL, "All other zones closed.");
    }


    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        WateringJob.commandExecutor = commandExecutor;
    }

    public static void setZones(Supplier<Set<String>> zones) {
        WateringJob.zones = zones;
    }


}
