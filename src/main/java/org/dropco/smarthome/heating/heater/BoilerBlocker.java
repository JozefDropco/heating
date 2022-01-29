package org.dropco.smarthome.heating.heater;

import org.dropco.smarthome.TimerService;
import org.dropco.smarthome.heating.ServiceMode;
import org.dropco.smarthome.heating.pump.SolarCircularPump;
import org.dropco.smarthome.heating.solar.HeatingConfiguration;
import org.dropco.smarthome.heating.ThreeWayValve;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class BoilerBlocker implements Runnable {

    public static final String BOILER_BLOCK_PIN = "BOILER_BLOCK_PIN";
    private static AtomicBoolean oneTimeManual = new AtomicBoolean(false);
    private static AtomicLong lastOneTimeStart = new AtomicLong();
    static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    static AtomicBoolean state = new AtomicBoolean(false);
    public static final Logger LOGGER = Logger.getLogger(BoilerBlocker.class.getName());

    public BoilerBlocker(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        SolarCircularPump.addSubscriber((state) -> update.release());
        ThreeWayValve.addSubscriber((state) -> update.release());
        HeatingConfiguration.addSubscriber(subs -> update.release());
        Boiler.addSubscriber(subs -> update.release());
    }

    @Override
    public void run() {
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                if (oneTimeManual.get()) {
                    if (state.compareAndSet(true, false)) {
                        LOGGER.info("Jednorázové ohriatie vody v nádobe spustené");
                        commandExecutor.accept(BOILER_BLOCK_PIN, false);
                    } else {
                        if (!Boiler.getState() && (System.currentTimeMillis() - lastOneTimeStart.get()) > 30000) {
                            oneTimeManual.compareAndSet(true, false);
                            normalFunctioning();
                            LOGGER.info("Jednorázový ohrev vody dokončený");
                        }
                    }
                } else {
                    normalFunctioning();
                }
            }
            update.acquireUninterruptibly();
        }
    }

    private void normalFunctioning() {
        if (SolarCircularPump.getState() && ThreeWayValve.getState() && state.compareAndSet(false, true)) {
            LOGGER.fine("Ohrev nádoby na vodu pomocou soláru, blokujem kotol");
            commandExecutor.accept(BOILER_BLOCK_PIN, true);
        } else {
            boolean boilerBlock = HeatingConfiguration.getCurrent().getBoilerBlock();
            if (state.compareAndSet(!boilerBlock, boilerBlock)) {
                LOGGER.fine("Ohrev nádoby na vodu " + ((boilerBlock) ? "zablokované" : "povolené"));
                commandExecutor.accept(BOILER_BLOCK_PIN, boilerBlock);
            }
        }
    }

    public static boolean getManualOverride() {
        return oneTimeManual.get();
    }

    public static void manualOverride() {
        if (oneTimeManual.compareAndSet(false, true)) {
            lastOneTimeStart.set(System.currentTimeMillis());
            TimerService.schedule("Delayed shutdown of one time manual", () -> update.release(),35000);
            update.release();
        } else {
            if (oneTimeManual.compareAndSet(true, false)) {
                update.release();
            }
        }
    }

    /***
     * Gets the state
     * @return
     */
    public static boolean getState() {
        return state.get();
    }
}
