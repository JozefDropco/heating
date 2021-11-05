package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.gpioextension.PulseInputGpioListener;
import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class VerticalMoveFeedback {
    private static final AtomicBoolean moving = new AtomicBoolean(false);
    private static final PinState LOGICAL_HIGH_STATE = PinState.LOW;
    private final List<Consumer<Boolean>> movingSubscribers = Collections.synchronizedList(Lists.newArrayList());
    private final List<Consumer<Boolean>> realTimeSubcribers = Collections.synchronizedList(Lists.newArrayList());

    public void start(GpioPinDigitalInput input) {
        input.addListener(new PulseInputGpioListener(LOGICAL_HIGH_STATE, 3000, input) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (moving.compareAndSet(false, state)) {
                        Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(state));
                    }
                } else {
                    if (moving.compareAndSet(true, state)) {
                        Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(state));
                    }
                }
            }
        });
        input.addListener((GpioPinListenerDigital) event -> Lists.newArrayList(realTimeSubcribers).forEach(sub -> sub.accept(event.getState() == LOGICAL_HIGH_STATE)));

    }


    /***
     * Gets the moving
     * @return
     */
    public static boolean getMoving() {
        return moving.get();
    }


    public void addSubscriber(Consumer<Boolean> consumer) {
        movingSubscribers.add(consumer);
    }

    public void addRealTimeTicker(Consumer<Boolean> consumer) {
        realTimeSubcribers.add(consumer);    }
}
