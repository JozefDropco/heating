package org.dropco.smarthome.heating.solar;

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

public class StrongWind {
    private static final AtomicBoolean isWindy = new AtomicBoolean(false);
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public static void connect(GpioPinDigitalInput strongWindPin) {
        StatsCollector.getInstance().collect("Silný vietor", strongWindPin, PinState.HIGH);
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.HIGH, 5000, strongWindPin) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (isWindy.compareAndSet(false, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.FINE, "Fúka silný vietor");
                        subscribers.forEach(consumer -> consumer.accept(state));
                    }
                } else {
                    if (isWindy.compareAndSet(true, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.FINE, "Skončil silný vietor");
                        subscribers.forEach(consumer -> consumer.accept(state));
                    }
                }
            }
        });
    }

    /***
     * Gets the maxContinuousLight
     * @return
     */
    public static boolean isWindy() {
        return isWindy.get();
    }


    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public static void removeSubscription(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }

    static void set(boolean state) {
        isWindy.set(state);
    }
}
