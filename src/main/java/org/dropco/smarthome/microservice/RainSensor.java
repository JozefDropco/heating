package org.dropco.smarthome.microservice;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.solar.StrongWind;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Collections;
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
        input.addListener(new DelayedGpioPinListener(RAIN_STATE,1000,input) {
                              @Override
                              public void handleStateChange(boolean state) {
                                  handleRainSensor(state);
                              }
                          });
        handleRainSensor(raining.get());

    }

    static void handleRainSensor(boolean newValue) {
        if (newValue) {
            if (raining.compareAndSet(false, newValue)) {
                logger.log(Level.INFO, "Prší");
                subscribers.forEach(subscriber -> {
                    try {
                        subscriber.accept(newValue);
                    } catch (RuntimeException ex) {
                        logger.log(Level.SEVERE, "Subscriber failed.", ex);
                    }
                });
            }
        } else {
            if (raining.compareAndSet(true, newValue)) {
                logger.log(Level.INFO, "Neprší");
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
