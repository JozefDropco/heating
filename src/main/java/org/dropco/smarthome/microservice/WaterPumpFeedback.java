package org.dropco.smarthome.microservice;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WaterPumpFeedback {
    public static final AtomicBoolean pumpOk = new AtomicBoolean(false);
    private static Logger logger = Logger.getLogger(WaterPumpFeedback.class.getName());
    private static final List<Consumer<Boolean>> subscribers = Lists.newArrayList();

    public static void start(GpioPinDigitalInput input) {
        pumpOk.set(input.getState() == PinState.HIGH);
        input.addListener((GpioPinListenerDigital) event -> {
                    handlePumpState(event.getState());
                }
        );
        handlePumpState(input.getState());

    }

    static void handlePumpState(PinState state) {
        boolean newValue = state == PinState.HIGH;
        boolean oldValue = pumpOk.getAndSet(newValue);
        if (pumpOk.get()) {
            logger.log(Level.INFO, "Water pump is OK");
        } else {
            logger.log(Level.INFO, "Water pump is not OK");
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

    public static boolean isPumpOk() {
        return pumpOk.get();
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
