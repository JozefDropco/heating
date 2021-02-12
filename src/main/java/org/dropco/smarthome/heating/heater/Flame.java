package org.dropco.smarthome.heating.heater;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Flame {
    public static final String HEATER_FLAME_REF_CD = "HEATER_FLAME_PORT";

    private GpioPinDigitalInput input;
    private static final AtomicBoolean state = new AtomicBoolean(false);
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public Flame(GpioPinDigitalInput input) {
        this.input = input;
    }

    public static Boolean getState() {
        return state.get();
    }


    public void start() {
        state.set(input.getState() == PinState.LOW);
        input.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.LOW) {
                boolean set = Flame.this.state.compareAndSet(false, true);
                if (set) subscribers.forEach(sub -> sub.accept(Flame.this.state.get()));
            } else {
                boolean set = Flame.this.state.compareAndSet(true, false);
                if (set) subscribers.forEach(sub -> sub.accept(Flame.this.state.get()));
            }
        });
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

}
