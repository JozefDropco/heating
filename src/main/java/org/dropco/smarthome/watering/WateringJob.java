package org.dropco.smarthome.watering;

import org.dropco.smarthome.ServiceMode;
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

    public static final String WATER_PUMP_WAIT_TIME = "WATER_PUMP_WAIT_TIME";
    public static final String RAIN_SENSOR = "RAIN_SENSOR";
    public static final String TEMP_THRESHOLD = "TEMP_THRESHOLD";
    public static final String WATER_PUMP_REF_CD = "WATER_PUMP";

    private static Supplier<Set<String>> zones;
    private static BiConsumer<String, Boolean> commandExecutor;
    private static Supplier<Boolean> raining;
    private static Supplier<Double> temperature;
    private static Consumer<Thread> watchPumpSupplier;

    private WateringRecord record;
    private static Supplier<Double> temperatureThreshold;

    WateringJob(WateringRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        long timeInSeconds = record.getTimeInSeconds();
        if (!ServiceMode.isServiceMode()) {
            if (!raining.get()) {
                if (temperature.get() > temperatureThreshold.get()) {
                    set(WATER_PUMP_REF_CD, record.getZoneRefCode(), true);
                    try {
                        Thread.sleep(1000);
                        closeZones(record.getZoneRefCode());
                        sleep(timeInSeconds);
                        if (!record.isContinuous()) {
                            set(WATER_PUMP_REF_CD, record.getZoneRefCode(), false);
                        }
                    } catch (InterruptedException ex) {
                        if (record.getHour() != record.getRetryHour() || record.getMinute() != record.getRetryMinute()) {
                            record.setHour(record.getRetryHour());
                            record.setMinute(record.getRetryMinute());
                            WateringScheduler.schedule(record);
                        }
                        set(WATER_PUMP_REF_CD, record.getZoneRefCode(), false);
                    }
                } else {
                    LOGGER.log(Level.FINE, "Temperature is too low for watering.");
                }
            } else
                LOGGER.log(Level.FINE, "It's raining.");
        } else {
            LOGGER.log(Level.FINE, "Service mode started, ignoring watering");
        }
    }

    void sleep(long timeInSeconds) throws InterruptedException {
        LOGGER.log(Level.FINE, "Sleeping for next " + timeInSeconds + " seconds.");
        watchPumpSupplier.accept(Thread.currentThread());
        Thread.sleep((timeInSeconds - 1) * 1000);
    }

    void set(String waterPumpRefCd, String zoneRefCode, boolean state) {
        commandExecutor.accept(zoneRefCode, state);
        commandExecutor.accept(waterPumpRefCd, state);
        LOGGER.log(Level.FINE, "Zone with REF_CD= " + record.getZoneRefCode() + (state ? " opened." : " closed."));

    }

    void closeZones(String zoneRefCode) {
        Set<String> allZones = zones.get();
        from(allZones).filter(zone -> !zone.equals(zoneRefCode)).forEach(z -> commandExecutor.accept(z, false));
        LOGGER.log(Level.FINE, "All zones closed.");
    }

    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        WateringJob.commandExecutor = commandExecutor;
    }

    public static void setZones(Supplier<Set<String>> zones) {
        WateringJob.zones = zones;
    }


    public static void setWatchPumpSupplier(Consumer<Thread> watchPumpSupplier) {
        WateringJob.watchPumpSupplier = watchPumpSupplier;
    }


    public static void setTemperature(Supplier<Double> temperature) {
        WateringJob.temperature = temperature;
    }

    public static void setTemperatureThreshold(Supplier<Double> temperatureThreshold) {
        WateringJob.temperatureThreshold = temperatureThreshold;
    }

    public static void setRaining(Supplier<Boolean> raining) {
        WateringJob.raining = raining;
    }
}
