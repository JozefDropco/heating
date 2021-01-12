package org.dropco.smarthome.solar.move;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarPanelThreadManager {
    private static final Logger LOGGER = Logger.getLogger(SolarPanelThreadManager.class.getName());
    public static Supplier<Long> delaySupplier;
    private static final AtomicReference<Thread> lastThread = new AtomicReference<>();

    public static void move(Integer horizontal, Integer vertical) {
        Thread thread = new Thread(new SolarPanelMover(horizontal, vertical));
        stop(thread);
        LOGGER.log(Level.INFO, "Natáčanie kolektorov na hor=" + horizontal + ", vert=" + vertical);
        Long delay = delaySupplier.get();
        LOGGER.log(Level.INFO, "Pauza na zastavenie motora " + delay + " sek.");
        try {
            Thread.sleep(delay * 1000);
            thread.start();

        } catch (InterruptedException e) {
            LOGGER.log(Level.FINE, "Interrupt occurred ", e);
        }
    }

    public static void stop() {
        stop(null);
    }

    static void stop(Thread thread) {
        Thread oldThread = lastThread.getAndSet(thread);
        if (oldThread != null) {
            LOGGER.log(Level.FINE, "Stopping the current thread - " + oldThread.getId());
            oldThread.interrupt();

            try {
                LOGGER.log(Level.FINE, "Waiting for current thread - " + oldThread.getId() + " to stop.");
                oldThread.join();
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "Waiting for current " + oldThread.getId() + " was interrupted.");
            }
        }
    }

}
