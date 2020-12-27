package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class DayLight {
    private static final AtomicInteger maxContinuousLight = new AtomicInteger(0);
    private static final AtomicLong lastHighStateTimestamp = new AtomicLong(0);
    private static Supplier<Integer> lightThreshold;

    public static void connect(GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        DayLight.lightThreshold = lightThreshold;
        if (input.isHigh()) lastHighStateTimestamp.set(System.currentTimeMillis());
        input.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if (event.getState().isHigh()) {
                    lastHighStateTimestamp.set(System.currentTimeMillis());
                } else {
                    long newValue = System.currentTimeMillis();
                    long previous = lastHighStateTimestamp.getAndSet(newValue);
                    if (previous != 0)
                        maxContinuousLight.getAndAccumulate((int) (newValue - previous), (left, right) -> Math.max(left, right));
                }
            }
        });

    }

    /***
     * Gets the maxContinuousLight
     * @return
     */
    public static boolean enoughLight() {
        return maxContinuousLight.get()>lightThreshold.get();
    }

    public static void clear() {
        maxContinuousLight.set(0);
    }
}
