package org.dropco.smarthome.heating.solar.move;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.PinManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MockedPinManager implements PinManager {
    Map<String, Boolean> states = new HashMap<>();
    Map<String, Set<Boolean>> allStates = new HashMap<>();

    @Override
    public GpioPinDigitalInput getInput(String key) {
        return null;
    }

    @Override
    public GpioPinDigitalOutput getOutput(String key) {
        return null;
    }

    @Override
    public PinState getState(String key) {
        Boolean state = states.get(key);
        if (state==null) state=Boolean.FALSE;
        return state ? PinState.HIGH : PinState.LOW;
    }

    public boolean wasState(String key, boolean state) {
        Set<Boolean> states = allStates.get(key);
        if (states==null) return false;
        return states.contains(state);
    }

    @Override
    public void setState(String key, boolean value) {
        states.put(key, value);
        allStates.computeIfAbsent(key, k-> Sets.newHashSet()).add(value);
    }

    @Override
    public Map<String, GpioPinDigitalInput> getInputMap() {
        return null;
    }

    @Override
    public Map<String, GpioPinDigitalOutput> getOutputMap() {
        return null;
    }
}
