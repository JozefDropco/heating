package org.dropco.smarthome.heating.heater;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.gpioextension.PulseInputGpioListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Boiler {
    public static final String HEATER_BOILER_FEC_CD = "HEATER_BOILER_PORT";


    private GpioPinDigitalInput input;
    private static final AtomicBoolean state = new AtomicBoolean(false);
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public Boiler(GpioPinDigitalInput input) {
        this.input = input;
        state.set(input.getState()==PinState.LOW);
    }

    public static Boolean getState() {
        return state.get();
    }


    public void start(long blinkStop) {
        input.addListener(new PulseInputGpioListener(PinState.LOW, blinkStop, input) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (Boiler.this.state.compareAndSet(false, state)) {
                        subscribers.forEach(sub -> sub.accept(state));
                    }
                } else {
                    if (Boiler.this.state.compareAndSet(true, state)) {
                        subscribers.forEach(sub -> sub.accept(state));
                    }
                }
            }
        });
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

}
