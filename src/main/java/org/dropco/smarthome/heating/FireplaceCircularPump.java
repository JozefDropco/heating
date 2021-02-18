package org.dropco.smarthome.heating;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FireplaceCircularPump {
    public static final String FIREPLACE_CIRCULAR_PUMP_REF_CD = "FIREPLACE_CIRCULAR_PUMP_PORT";

    private GpioPinDigitalInput input;
    private static final AtomicBoolean state = new AtomicBoolean(false);
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public FireplaceCircularPump(GpioPinDigitalInput input) {
        this.input = input;
    }

    public static Boolean getState() {
        return state.get();
    }


    public void start() {
        input.setPullResistance(PinPullResistance.PULL_UP);
        state.set(input.getState() == PinState.LOW);
        input.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.LOW) {
                boolean set = FireplaceCircularPump.this.state.compareAndSet(false, true);
                if (set) subscribers.forEach(sub -> sub.accept(FireplaceCircularPump.this.state.get()));
            } else {
                boolean set = FireplaceCircularPump.this.state.compareAndSet(true, false);
                if (set) subscribers.forEach(sub -> sub.accept(FireplaceCircularPump.this.state.get()));
            }
        });
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

}
