package org.dropco.smarthome.watering;

import org.dropco.smarthome.dto.NamedPort;
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

    private static Supplier<String> waterPumpPort;
    private static Supplier<Long> sleepBeforeCloseOfWaterPump;
    private static Supplier<Set<NamedPort>> zones;
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
                LOGGER.log(Level.INFO, "Čerpadlo nebeží, vypínam zónu " + record.getName());
                thisThread.interrupt();
            }
        }
    };
    private Consumer<Boolean> pumpSubscriber = running -> {
        if (!running) {
            LOGGER.log(Level.INFO, "Čerpadlo nebeží, vypínam zónu " + record.getName());
            thisThread.interrupt();
        }
    };

    WateringJob(WateringRecord record) {
        this.record = record;
    }

    @Override
    public void run() {
        thisThread = Thread.currentThread();
        String waterPumpPort = WateringJob.waterPumpPort.get();
        long before = System.currentTimeMillis();
        try {
            RainSensor.subscribe(rainSubscriber);
            set(record.getZoneRefCode(), !RainSensor.isRaining());
            closeOtherZones(record.getZoneRefCode());
            commandExecutor.accept(waterPumpPort, true);
            sleep(3);
            if (!WaterPumpFeedback.getRunning()) {
                LOGGER.log(Level.INFO, "Čerpadlo nebeží, vypínam zónu " + record.getName());
                thisThread.interrupt();
            }
            WaterPumpFeedback.subscribe(pumpSubscriber);
            sleep(record.getTimeInSeconds() - 3);
            set(record.getZoneRefCode(), false);
        } catch (InterruptedException ex) {
            if (!WaterPumpFeedback.getRunning()) {
                int elapsedSeconds = Math.abs((int) ((System.currentTimeMillis() - before) / 1000));
                WateringThreadManager.tryReschedule(record, elapsedSeconds);
                set(record.getZoneRefCode(), false);
            }
        } finally {
            RainSensor.unsubscribe(rainSubscriber);
            WaterPumpFeedback.unsubscribe(pumpSubscriber);
            try {
                Thread.sleep(1000 * sleepBeforeCloseOfWaterPump.get());
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Sleep interrupted");
            } finally {
                commandExecutor.accept(waterPumpPort, false);
            }
        }
    }


    void sleep(long timeInSeconds) throws InterruptedException {
        LOGGER.log(Level.INFO, "Polievanie " + timeInSeconds + " sekúnd.");
        Thread.sleep(timeInSeconds * 1000);
    }

    void set(String zoneRefCode, boolean state) {
        commandExecutor.accept(zoneRefCode, state);
        LOGGER.log(LEVEL, "Zóna " + record.getName() + (state ? " otvorená." : " zatvorená."));

    }

    void closeOtherZones(String zoneRefCode) {
        Set<NamedPort> allZones = zones.get();
        from(allZones).filter(zone -> !zone.getRefCd().equals(zoneRefCode)).forEach(z -> commandExecutor.accept(z.getRefCd(), false));
        LOGGER.log(LEVEL, "Ostatné zóny uzatvorené.");
    }


    public static void setCommandExecutor(BiConsumer<String, Boolean> commandExecutor) {
        WateringJob.commandExecutor = commandExecutor;
    }

    public static void setZones(Supplier<Set<NamedPort>> zones) {
        WateringJob.zones = zones;
    }

    public static void setWaterPumpPort(Supplier<String> waterPumpPort) {
        WateringJob.waterPumpPort = waterPumpPort;
    }

    public static void setSleepBeforeCloseOfWaterPump(Supplier<Long> sleepBeforeCloseOfWaterPump) {
        WateringJob.sleepBeforeCloseOfWaterPump = sleepBeforeCloseOfWaterPump;
    }
}
