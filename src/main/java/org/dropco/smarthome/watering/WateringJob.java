package org.dropco.smarthome.watering;

import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.watering.db.WateringRecord;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.collect.FluentIterable.from;

public class WateringJob implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(WateringJob.class.getName());

    public static final String WATER_PUMP_WAIT_TIME = "WATER_PUMP_WAIT_TIME";
    public static final String RAIN_SENSOR = "RAIN_SENSOR";
    public static final String TEMP_THRESHOLD = "TEMP_THRESHOLD";
    public static final String WATER_PUMP_REF_CD = "WATER_PUMP";
    public static final String WATER_PUMP_FEEDBACK_REF_CD = "WATER_PUMP_FEEDBACK";
    public static final Level LEVEL = Level.INFO;

    private static Supplier<Set<String>> zones;
    private static BiConsumer<String, Boolean> commandExecutor;
    private static BiConsumer<AtomicBoolean, Thread> watch;
    private AtomicBoolean noWater = new AtomicBoolean(false);
    private WateringRecord record;

    WateringJob(WateringRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        long timeInSeconds = record.getTimeInSeconds();
        if (!ServiceMode.isServiceMode()) {
            try {
                watch.accept(noWater,Thread.currentThread());
                set(WATER_PUMP_REF_CD, record.getZoneRefCode(), true);
                closeZones(record.getZoneRefCode());
                sleep(timeInSeconds - 1);
                if (!record.isContinuous()) {
                    set(WATER_PUMP_REF_CD, record.getZoneRefCode(), false);
                }
            } catch (InterruptedException ex) {
                if (noWater.get()) {
                    LOGGER.log(Level.INFO, "Sleep interrupted. Retry mechanism in action");
                    if (record.getHour() != record.getRetryHour() || record.getMinute() != record.getRetryMinute()) {
                        record.setHour(record.getRetryHour());
                        record.setMinute(record.getRetryMinute());
                        WateringScheduler.schedule(record);
                    }
                    try {
                        set(WATER_PUMP_REF_CD, record.getZoneRefCode(), false);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } else {
            LOGGER.log(Level.INFO, "Service mode started, ignoring watering");
        }
    }

    void sleep(long timeInSeconds) throws InterruptedException {
        LOGGER.log(Level.INFO, "Sleeping for next " + timeInSeconds + " seconds.");
        Thread.sleep(timeInSeconds * 1000);
    }

    void set(String waterPumpRefCd, String zoneRefCode, boolean state) throws InterruptedException {
        if (state) {
            commandExecutor.accept(waterPumpRefCd, state);
            Thread.sleep(1000);
            commandExecutor.accept(zoneRefCode, state);
        } else {
            commandExecutor.accept(zoneRefCode, state);
            Thread.sleep(1000);
            commandExecutor.accept(waterPumpRefCd, state);
        }
        LOGGER.log(LEVEL, "Zone with REF_CD= " + record.getZoneRefCode() + (state ? " opened." : " closed."));

    }

    void closeZones(String zoneRefCode) {
        Set<String> allZones = zones.get();
        from(allZones).filter(zone -> !zone.equals(zoneRefCode)).forEach(z -> commandExecutor.accept(z, false));
        LOGGER.log(LEVEL, "All zones closed.");
    }

    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        WateringJob.commandExecutor = commandExecutor;
    }

    public static void setZones(Supplier<Set<String>> zones) {
        WateringJob.zones = zones;
    }


    public static void setWatch(BiConsumer<AtomicBoolean, Thread> watch) {
        WateringJob.watch = watch;
    }

}
