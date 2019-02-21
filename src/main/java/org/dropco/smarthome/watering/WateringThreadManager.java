package org.dropco.smarthome.watering;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WateringThreadManager {
    private static final Logger LOGGER = Logger.getLogger(WateringThreadManager.class.getName());
    private static final AtomicReference<Thread> lastThread = new AtomicReference<>();

    static void move(String zone, long timeInSeconds) {
        Thread thread = new Thread(new WateringJob(zone,timeInSeconds));
        stop(thread);
        LOGGER.log(Level.INFO,"Starting new thread with id="+thread.getId()+" which will start to water zone="+zone+", seconds="+timeInSeconds);
        thread.start();
    }

    public static void stop() {
        stop(null);
    }
    private static void stop(Thread thread) {
        Thread oldThread = lastThread.getAndSet(thread);
        if (oldThread != null) {
            LOGGER.log(Level.INFO,"Stopping the current thread - "+oldThread.getId());
            oldThread.interrupt();

            try {
                LOGGER.log(Level.INFO,"Waiting for current thread - "+oldThread.getId()+" to stop.");
                oldThread.join();
            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE,"Waiting for current "+oldThread.getId()+" was interrupted.");
            }
        }
    }

}
