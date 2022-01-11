package org.dropco.smarthome.heating.solar;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
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
        strongWindPin.setPullResistance(PinPullResistance.PULL_UP);
        strongWindPin.setDebounce(1000);
        StatsCollector.getInstance().collect("Silný vietor", strongWindPin, PinState.LOW);
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.LOW, 5000, strongWindPin) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (isWindy.compareAndSet(false, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.INFO, "Fúka silný vietor");
                        subscribers.forEach(consumer -> consumer.accept(state));
                    }
                } else {
                    if (isWindy.compareAndSet(true, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.INFO, "Skončil silný vietor");
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
