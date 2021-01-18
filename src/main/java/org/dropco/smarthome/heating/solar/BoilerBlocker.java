package org.dropco.smarthome.heating.solar;

import org.dropco.smarthome.ServiceMode;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class BoilerBlocker implements Runnable {

    static final String BOILER_BLOCK_PIN = "BOILER_BLOCK_PIN";
    static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    static AtomicBoolean state = new AtomicBoolean(false);
    public static final Logger LOGGER = Logger.getLogger(BoilerBlocker.class.getName());

    public BoilerBlocker(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        SolarCircularPump.addSubscriber((state) -> update.release());
        ThreeWayValve.addSubscriber((state) -> update.release());
        SolarHeatingCurrentSetup.addSubscriber(subs -> update.release());
    }

    @Override
    public void run() {
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                if (SolarCircularPump.getState() && ThreeWayValve.getState() && state.compareAndSet(false, true)) {
                    LOGGER.fine("Ohrev nádoby na vodu pomocou soláru, blokujem kotol");
                    commandExecutor.accept(BOILER_BLOCK_PIN, true);
                } else {
                    boolean boilerBlock = SolarHeatingCurrentSetup.get().getBoilerBlock();
                    if (state.compareAndSet(!boilerBlock, boilerBlock)) {
                        LOGGER.fine("Ohrev nádoby na vodu " + ((boilerBlock) ? "zablokované" : "povolené"));
                        commandExecutor.accept(BOILER_BLOCK_PIN, boilerBlock);
                    }
                }
            }
            update.acquireUninterruptibly();
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
