package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StrongWind {
    private static final AtomicBoolean isWindy = new AtomicBoolean(false);

    public static void connect(GpioPinDigitalInput strongWindPin, SafetySolarPanel safetySolarPanel) {
        StatsCollector.getInstance().collect("Silný vietor", strongWindPin, PinState.LOW);
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.LOW, 5000, strongWindPin) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (isWindy.compareAndSet(false, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.FINE, "Fúka silný vietor");
                        safetySolarPanel.moveToStrongWindPosition();
                    }
                } else {
                    if (isWindy.compareAndSet(true, state)) {
                        Logger.getLogger(StrongWind.class.getName()).log(Level.FINE, "Skončil silný vietor");
                        safetySolarPanel.backToNormal();
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

    static void set(boolean state) {
        isWindy.set(state);
    }
}
