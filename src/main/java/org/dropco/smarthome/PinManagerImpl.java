package org.dropco.smarthome;

import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PinManagerImpl implements PinManager {
    private Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());

    private static GpioController gpio = GpioFactory.getInstance();



    public GpioPinDigitalInput getInput(String key) {
        String pinName = Db.applyDao(new SettingsDao(), dao -> dao.getString(key));
        GpioPinDigitalInput input = inputMap.get(pinName);
        if (input == null) {
            input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), key);
            inputMap.put(pinName, input);
        }
        return input;
    }

    public GpioPinDigitalOutput getOutput(String key) {
        return getOutput(GpioFactory.getDefaultProvider(), RaspiPin.class, key);
    }

    @Override
    public PinState getState(String key) {
        GpioPinDigitalOutput output = getOutput(key);
        if (output!=null) return output.getState();
        return null;
    }

    @Override
    public void setState(String key, boolean value) {
        getOutput(key).setState(value);
    }

    GpioPinDigitalOutput getOutput(GpioProvider extendedProvider, Class<? extends PinProvider> pinClass, String key) {
        String pinName = Db.applyDao(new SettingsDao(), dao -> dao.getString(key));
        GpioPinDigitalOutput output = outputMap.get(pinName);
        if (output == null) {
            Pin pin = null;
            try {
                pin = (Pin) pinClass.getMethod("getPinByName", String.class).invoke(null, pinName);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            output = gpio.provisionDigitalOutputPin(extendedProvider, pin, key, PinState.LOW);
            outputMap.put(pinName, output);
        }
        return output;
    }

    public Map<String, GpioPinDigitalOutput> getOutputMap() {
        return outputMap;
    }

    public Map<String, GpioPinDigitalInput> getInputMap() {
        return inputMap;
    }
}
