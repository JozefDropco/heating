package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class DayLight {
    private static final AtomicBoolean enoughLight = new AtomicBoolean(false);

    public static void connect(GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        input.addListener(new DelayedGpioPinListener(PinState.HIGH, lightThreshold.get(), input) {


            @Override
            public void handleStateChange(boolean state) {
                enoughLight.compareAndSet(false, state);
            }
        });

    }

    /***
     * Gets the maxContinuousLight
     * @return
     */
    public static boolean enoughLight() {
        return enoughLight.get();
    }

    public static void clear() {
        enoughLight.set(false);
    }
}
