package org.dropco.smarthome.heating.heater;

import com.google.common.collect.Lists;
import org.dropco.smarthome.TimerService;
import org.dropco.smarthome.heating.ServiceMode;
import org.dropco.smarthome.heating.pump.SolarCircularPump;
import org.dropco.smarthome.heating.ThreeWayValve;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class BoilerBlocker implements Runnable {

    private static AtomicBoolean holidayMode = new AtomicBoolean(false);
    private static AtomicBoolean oneTimeManual = new AtomicBoolean(false);
    private static AtomicLong lastOneTimeStart = new AtomicLong();
    static final Semaphore update = new Semaphore(0);
    public static BoilerBlockerRelay boilerBlockerRelay;
    static AtomicBoolean state = new AtomicBoolean(false);
    public static final Logger LOGGER = Logger.getLogger(BoilerBlocker.class.getName());
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public BoilerBlocker(BiConsumer<String, Boolean> commandExecutor) {
        boilerBlockerRelay = new BoilerBlockerRelay(commandExecutor);
        SolarCircularPump.addSubscriber((state) -> update.release());
        ThreeWayValve.addSubscriber((state) -> update.release());
        HeatingConfiguration.addSubscriber(subs -> update.release());
        Boiler.addSubscriber(subs -> update.release());
        ServiceMode.addSubsriber((state) -> update.release());
    }

    @Override
    public void run() {
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                if (oneTimeManual.get()) {
                    if (state.compareAndSet(true, false)) {
                        LOGGER.info("Jednorázové ohriatie vody v nádobe spustené");
                        boilerBlockerRelay.stopBlocking();
                        raiseEvent();
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
        if (holidayMode.get()) {
            if (state.compareAndSet(false, true)) {
                LOGGER.fine("Ohrev nádoby na vodu blokovaný pre prázdninový mód");
                boilerBlockerRelay.startBlocking();
                raiseEvent();
            }
        } else {
            if (SolarCircularPump.getState() && ThreeWayValve.getState()) {
                if (state.compareAndSet(false, true)) {
                   LOGGER.fine("Ohrev nádoby na vodu pomocou soláru, blokujem kotol");
                   boilerBlockerRelay.startBlocking();
                   raiseEvent();
                }
            } else {
                boolean boilerBlock = HeatingConfiguration.getCurrent().getBoilerBlock();
                if (state.compareAndSet(!boilerBlock, boilerBlock)) {
                    if (boilerBlock) {
                        LOGGER.fine("Ohrev nádoby na vodu zablokované");
                        boilerBlockerRelay.startBlocking();
                    } else {
                        LOGGER.fine("Ohrev nádoby na vodu povolené");
                        boilerBlockerRelay.stopBlocking();
                    }
                    raiseEvent();
                }
            }
        }
    }


    public static boolean getManualOverride() {
        return oneTimeManual.get();
    }

    public static void manualOverride() {
        if (oneTimeManual.compareAndSet(false, true)) {
            lastOneTimeStart.set(System.currentTimeMillis());
            TimerService.schedule("Delayed shutdown of one time manual", () -> update.release(), 35000);
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

    public static void setState(boolean state) {
        if (BoilerBlocker.state.compareAndSet(!state, state)) {
            if (state)
                boilerBlockerRelay.startBlocking();
            else
                boilerBlockerRelay.stopBlocking();
            raiseEvent();
        }
    }

    public static void setHolidayMode(boolean holidayMode) {
        BoilerBlocker.holidayMode.set(holidayMode);
        update.release();
    }

    /***
     * Gets the holidayMode
     * @return
     */
    public static boolean getHolidayMode() {
        return holidayMode.get();
    }


    private static void raiseEvent() {
        subscribers.forEach(sub -> sub.accept(state.get()));
    }


    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

}
