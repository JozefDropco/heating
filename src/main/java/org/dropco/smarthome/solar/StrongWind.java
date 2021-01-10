package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class StrongWind {
    private static final AtomicBoolean isWindy = new AtomicBoolean(false);

    public static void connect(GpioPinDigitalInput strongWindPin, SafetySolarPanel safetySolarPanel) {
        StatsCollector.getInstance().collect("Silný vietor",strongWindPin);
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.HIGH, 5000, strongWindPin) {
            @Override
            public void handleStateChange(boolean state) {
                isWindy.set(state);
                if (state) {
                    safetySolarPanel.moveToStrongWindPosition();
                } else {
                    safetySolarPanel.backToNormal();
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

     static void set(boolean state) {
        isWindy.set(state);
    }
}
