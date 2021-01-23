package org.dropco.smarthome.microservice;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaterPumpFeedback {
    public static final AtomicBoolean running = new AtomicBoolean(false);
    private static Logger logger = Logger.getLogger(WaterPumpFeedback.class.getName());
    private static final List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public static void start(GpioPinDigitalInput input) {
        StatsCollector.getInstance().collect("Čerpadlo zavlažovania",input);
        running.set(input.getState() == PinState.HIGH);
        input.setDebounce(1000);
        input.addListener((GpioPinListenerDigital) event -> {
                    handlePumpState(event.getState());
                }
        );
        handlePumpState(input.getState());

    }

    static void handlePumpState(PinState state) {
        boolean newValue = state == PinState.HIGH;
        boolean oldValue = running.getAndSet(newValue);
        if (running.get()) {
            logger.log(Level.INFO, "Čerpadlo beží");
        } else {
            logger.log(Level.INFO, "Čerpadlo nebeží");
        }
        if (newValue != oldValue) {
            subscribers.forEach(subscriber -> {
                try {
                    subscriber.accept(newValue);
                } catch (RuntimeException ex) {
                    logger.log(Level.SEVERE, "Subscriber failed.", ex);
                }
            });
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
