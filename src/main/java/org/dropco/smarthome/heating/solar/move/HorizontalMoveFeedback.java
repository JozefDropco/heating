package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import org.dropco.smarthome.gpioextension.PulseInputGpioListener;
import org.dropco.smarthome.gpioextension.RemovableGpioPinListenerDigital;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HorizontalMoveFeedback {
    private static final AtomicBoolean moving = new AtomicBoolean(false);
    private static final PinState LOGICAL_HIGH_STATE = PinState.LOW;
    private GpioPinDigitalInput input;
    private final List<BiConsumer<Supplier<Boolean>,Boolean>> movingSubscribers = Collections.synchronizedList(Lists.newArrayList());
    private static final HorizontalMoveFeedback instance = new HorizontalMoveFeedback();

    public void start() {
        input.addListener(new PulseInputGpioListener(LOGICAL_HIGH_STATE, 3000, input) {
            @Override
            public void handleStateChange(boolean state) {
                if (state) {
                    if (moving.compareAndSet(false, state)) {
                        Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(()-> movingSubscribers.remove(sub),state));
                    }
                } else {
                    if (moving.compareAndSet(true, state)) {
                        Lists.newArrayList(movingSubscribers).forEach(sub -> sub.accept(()-> movingSubscribers.remove(sub),state));
                    }
                }
            }
        });
    }

    /***
     * Gets the instance
     * @return
     */
    public static HorizontalMoveFeedback getInstance() {
        return instance;
    }

    public HorizontalMoveFeedback setInput(GpioPinDigitalInput input) {
        this.input = input;
        return this;
    }

    /***
     * Gets the input
     * @return
     */
    public GpioPinDigitalInput getInput() {
        return input;
    }

    /***
     * Gets the moving
     * @return
     */
    public static boolean getMoving() {
        return moving.get();
    }


    public void addSubscriber(BiConsumer<Supplier<Boolean>,Boolean> consumer) {
        movingSubscribers.add(consumer);
    }

    public RemovableGpioPinListenerDigital addRealTimeTicker(Consumer<Boolean> consumer) {
        RemovableGpioPinListenerDigital listener = new RemovableGpioPinListenerDigital(input) {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                consumer.accept(event.getState() == LOGICAL_HIGH_STATE);
            }
        };
        input.addListener(listener);
        return listener;
    }
}
