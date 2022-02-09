package org.dropco.smarthome.watering;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaterPumpFeedback {
    public static final AtomicBoolean running = new AtomicBoolean(false);
    public static final PinState LOGICAL_HIGH_STATE = PinState.HIGH;
    private static Logger logger = Logger.getLogger(WaterPumpFeedback.class.getName());
    private static final List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public static void start(GpioPinDigitalInput input) {
        StatsCollector.getInstance().collect("Čerpadlo zavlažovania",input);
        running.set(input.getState() == LOGICAL_HIGH_STATE);
        input.setDebounce(1000);
        input.addListener(new DelayedGpioPinListener(PinState.HIGH,1000,input) {
            @Override
            public void handleStateChange(boolean state) {
                handlePumpState(state);
            }
        });
        handlePumpState(running.get());

    }

    static void handlePumpState(boolean newValue) {
        if (newValue){
            if (running.compareAndSet(false, newValue)) {
                logger.log(Level.INFO, "Čerpadlo beží");
                subscribers.forEach(subscriber -> {
                    try {
                        subscriber.accept(newValue);
                    } catch (RuntimeException ex) {
                        logger.log(Level.SEVERE, "Subscriber failed.", ex);
                    }
                });
            }
        } else {
            if (running.compareAndSet(true, newValue)) {
                logger.log(Level.INFO, "Čerpadlo nebeží");
                subscribers.forEach(subscriber -> {
                    try {
                        subscriber.accept(newValue);
                    } catch (RuntimeException ex) {
                        logger.log(Level.SEVERE, "Subscriber failed.", ex);
                    }
                });
            }
        }
    }

    public static boolean getRunning() {
        return running.get();
    }

    public static void subscribe(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public static void unsubscribe(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }

    public static String getMicroServicePinKey() {
        return "WATER_PUMP_FEEDBACK";
    }

}
