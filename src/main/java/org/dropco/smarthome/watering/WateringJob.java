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

    private static Supplier<Set<NamedPort>> zones;
    private static BiConsumer<String, Boolean> commandExecutor;
    private Thread thisThread;
    private WateringRecord record;
    private Consumer<Boolean> rainSubscriber = isRaining -> {
        if (!isRaining) {
            set(record.getZoneRefCode(), true);
            try {
                sleep(3);
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Sleep interrupted");
            }
            if (!WaterPumpFeedback.getRunning()) {
                LOGGER.log(Level.INFO, "Čerpadlo nebeží, vypínam zónu " + record.getName());
                thisThread.interrupt();
            }
        } else {
            set(record.getZoneRefCode(), false);
            thisThread.interrupt();
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
        long before = System.currentTimeMillis();
        try {
            RainSensor.subscribe(rainSubscriber);
            set(record.getZoneRefCode(), !RainSensor.isRaining());
            closeOtherZones(record.getZoneRefCode());
            sleep(5);
            if (!WaterPumpFeedback.getRunning()) {
                LOGGER.log(Level.INFO, "Čerpadlo nebeží, vypínam zónu " + record.getName());
                thisThread.interrupt();
            }
            WaterPumpFeedback.subscribe(pumpSubscriber);
            sleep(record.getTimeInSeconds() - 5);
            set(record.getZoneRefCode(), false);
        } catch (InterruptedException ex) {
            if (!WaterPumpFeedback.getRunning()) {
                int elapsedSeconds = Math.abs((int) ((System.currentTimeMillis() - before) / 1000));
                WateringThreadManager.tryReschedule(record, elapsedSeconds);
            }
        } finally {
            set(record.getZoneRefCode(), false);
            RainSensor.unsubscribe(rainSubscriber);
            WaterPumpFeedback.unsubscribe(pumpSubscriber);
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

}
