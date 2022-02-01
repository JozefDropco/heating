package org.dropco.smarthome.heating.solar.move.horizontal;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.gpioextension.PulseInputGpioListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class HorizontalMoveFeedback {
    private final AtomicBoolean moving = new AtomicBoolean(false);
    private AtomicInteger tickCount = new AtomicInteger();
    private static final PinState LOGICAL_HIGH_STATE = PinState.LOW;
    private final List<Consumer<Boolean>> movingSubscribers = Collections.synchronizedList(Lists.newArrayList());
    private final List<Consumer<Boolean>> realTimeSubcribers = Collections.synchronizedList(Lists.newArrayList());
    private PulseInputGpioListener pulseInputGpioListener;

    public void start(GpioPinDigitalInput input) {
        pulseInputGpioListener = new PulseInputGpioListener(LOGICAL_HIGH_STATE, 2000, input) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (moving.compareAndSet(false, true)) {
                        Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(true));
                    }
                } else {
                    moving.set(false);
                    Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(false));
                }
            }
        };
        input.addListener(pulseInputGpioListener);
        input.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == LOGICAL_HIGH_STATE) tickCount.incrementAndGet();
        });
        input.addListener((GpioPinListenerDigital) event -> Lists.newArrayList(realTimeSubcribers).forEach(sub -> sub.accept(event.getState() == LOGICAL_HIGH_STATE)));

    }


    /***
     * Gets the moving
     * @return
     */
    public boolean isMoving() {
        return moving.get();
    }


    public void addMovingSubscriber(Consumer<Boolean> consumer) {
        movingSubscribers.add(consumer);
    }

    public void addRealTimeTicker(Consumer<Boolean> consumer) {
        realTimeSubcribers.add(consumer);
    }

    /***
     * Gets the tickCount
     * @return
     */
    public int getTickCount() {
        return tickCount.get();
    }

    public void wakeUpWatch() {
        pulseInputGpioListener.wakeUpWatchThread();
    }
}
