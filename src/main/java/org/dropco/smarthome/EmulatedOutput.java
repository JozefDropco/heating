package org.dropco.smarthome;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class EmulatedOutput implements ServiceOutput {
    private Supplier<Boolean> getState;
    private Consumer<Boolean> setState;


    public EmulatedOutput(Supplier<Boolean> getState, Consumer<Boolean> setState) {
        this.getState = getState;
        this.setState = setState;
    }

    @Override
    public boolean getState() {
        return getState.get();
    }

    @Override
    public void setState(boolean state) {
        setState.accept(state);
    }
}
