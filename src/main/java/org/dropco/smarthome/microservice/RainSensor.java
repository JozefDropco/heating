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

public class RainSensor {
    private static final AtomicBoolean raining = new AtomicBoolean(false);
    private static final PinState RAIN_STATE = PinState.LOW;
    private static Logger logger = Logger.getLogger(RainSensor.class.getName());
    private static final List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public static void start(GpioPinDigitalInput input) {
        StatsCollector.getInstance().collect("Dážď",input);
        raining.set(input.getState() == RAIN_STATE);
        input.setDebounce(1000);
        input.addListener((GpioPinListenerDigital) event -> {
                    handleRainSensor(event.getState());
                }
        );
        handleRainSensor(input.getState());

    }

    static void handleRainSensor(PinState state) {
        boolean newValue = state == RAIN_STATE;
        boolean oldValue = raining.getAndSet(newValue);
        if (raining.get()) {
            logger.log(Level.INFO, "Prší");
        } else {
            logger.log(Level.INFO, "Neprší");
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

    public static boolean isRaining() {
        return raining.get();
    }

    public static void subscribe(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public static void unsubscribe(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }

    public static String getMicroServicePinKey() {
        return "RAIN_SENSOR";
    }
}
