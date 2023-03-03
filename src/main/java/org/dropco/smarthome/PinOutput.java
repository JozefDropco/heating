package org.dropco.smarthome;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import java.util.function.Function;

public class PinOutput implements ServiceOutput {
    private String refCd;
    private Function<String, GpioPinDigitalOutput> portGetter;

    public PinOutput(String refCd, Function<String, GpioPinDigitalOutput> portGetter) {
        this.refCd = refCd;
        this.portGetter = portGetter;
    }

    @Override
    public boolean getState() {
        return portGetter.apply(refCd).isState(PinState.HIGH);
    }

    @Override
    public void setState(boolean state) {
        portGetter.apply(refCd).setState(state);
    }
}
