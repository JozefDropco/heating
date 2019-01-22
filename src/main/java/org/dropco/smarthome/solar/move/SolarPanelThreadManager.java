package org.dropco.smarthome.solar.move;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarPanelThreadManager {
    private static final Logger LOGGER = Logger.getLogger(SolarPanelThreadManager.class.getName());
    private static final AtomicReference<Thread> lastThread = new AtomicReference<>();

    static void move(Integer horizontal, Integer vertical) {
        Thread thread = new Thread(new SolarPanelMover(horizontal, vertical));
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
        LOGGER.log(Level.INFO,"Starting new thread with id="+thread.getId()+" which will start to move to horizontal="+horizontal+", vertical="+vertical);
        thread.start();
    }

}
