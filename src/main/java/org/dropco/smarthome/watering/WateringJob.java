package org.dropco.smarthome.watering;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WateringJob implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(WateringJob.class.getName());
    private static Supplier<Set<String>> zones;
    private static BiConsumer<String, Boolean> commandExecutor;
    private String currentZone;
    private long timeInSeconds;

    WateringJob(String currentZone, long timeInSeconds) {
        this.currentZone = currentZone;
        this.timeInSeconds = timeInSeconds;
    }

    @Override
    public void run() {
        String WATER_PUMP_REF_CD = "WATER_PUMP";
        Set<String> allZones = zones.get();
        allZones.forEach(z -> commandExecutor.accept(z, false));
        LOGGER.log(Level.FINE,"All zones closed.");
        commandExecutor.accept(currentZone, true);
        commandExecutor.accept(WATER_PUMP_REF_CD, true);
        LOGGER.log(Level.FINE,"Zone with REF_CD= " +currentZone +" opened.");
        try {
            LOGGER.log(Level.FINE,"Sleeping for next "+ timeInSeconds +" seconds.");
            Thread.sleep(timeInSeconds*1000);
        } catch (InterruptedException e) {
            LOGGER.log(Level.FINE,"Sleep interrupted");
        }
        commandExecutor.accept(WATER_PUMP_REF_CD,false);
        commandExecutor.accept(currentZone,false);
        LOGGER.log(Level.FINE,"Zone with REF_CD= " +currentZone +" closed.");
    }

    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        WateringJob.commandExecutor = commandExecutor;
    }

    public static void setZones(Supplier<Set<String>> zones) {
        WateringJob.zones = zones;
    }
}
