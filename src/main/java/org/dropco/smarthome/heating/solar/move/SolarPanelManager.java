package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.Position;
import org.dropco.smarthome.heating.solar.dto.PositionProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarPanelManager {
    private static final Logger LOGGER = Logger.getLogger(SolarPanelManager.class.getName());
    public static Supplier<Long> delaySupplier;
    private static final AtomicReference<Thread> lastThread = new AtomicReference<>();
    private static List<Consumer<Consumer<Void>>> stopListeners = Collections.synchronizedList(new ArrayList<>());

    public static void move(Position position) {
        Thread thread = new Thread(new SolarPanelMover(position));
        stop(thread);
        position.invoke(new PositionProcessor() {
            @Override
            public void process(AbsolutePosition absPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov na hor=" + absPos.getHorizontal() + ", vert=" + absPos.getVertical());
            }

            @Override
            public void process(DeltaPosition deltaPos) {
                LOGGER.log(Level.FINE, "Natáčanie kolektorov o hor=" + deltaPos.getDeltaHorizontalTicks() + ", vert=" + deltaPos.getDeltaVerticalTicks());
            }
        });
        Long delay = delaySupplier.get();
        LOGGER.log(Level.FINE, "Pauza na zastavenie motora " + delay + " sek.");
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
                Lists.newArrayList(stopListeners).forEach(listener-> listener.accept((avoid)->SolarPanelManager.removeStopListener(listener)));
            } catch (InterruptedException e) {
                LOGGER.log(Level.FINE, "Waiting for current " + oldThread.getId() + " was interrupted.");
            }
        }
    }

    public static void addStopListener(Consumer<Consumer<Void>> stopListener){
        stopListeners.add(stopListener);
    }
    public static void removeStopListener(Consumer<Consumer<Void>> listener){
        stopListeners.remove(listener);
    }
}
