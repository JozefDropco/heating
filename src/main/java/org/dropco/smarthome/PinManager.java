package org.dropco.smarthome;

import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

import java.util.Map;

public interface PinManager {
    GpioPinDigitalInput getInput(String key);

    GpioPinDigitalOutput getOutput(String key);

    PinState getState(String key);

    void setState(String key, boolean value);

    Map<String, GpioPinDigitalInput> getInputMap();

    Map<String, GpioPinDigitalOutput> getOutputMap();
}
