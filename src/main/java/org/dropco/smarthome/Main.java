package org.dropco.smarthome;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.HeatingWorker;
import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.solar.SolarMain;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.temp.TempService;
import org.dropco.smarthome.watering.WateringMain;
import org.dropco.smarthome.web.WebServer;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLOutput;
import java.util.*;
import java.util.logging.Logger;


public class Main {


    private static final SettingsDao settingsDao = new SettingsDao();
    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());

    private static GpioController gpio = GpioFactory.getInstance();

    public static void main(String[] args) throws Exception {
        // Create JAX-RS application.
        new Thread(new TempService()).start();
        WebServer webServer = new WebServer();
        webServer.start();
        Logger.getLogger("").addHandler(new LogHandler());
        StatsCollector.getInstance().start();
        Set<String> inputs = Sets.newHashSet(args);
        if (!inputs.contains("--noWatering")) {
            ServiceMode.addInput(new NamedPort(WaterPumpFeedback.getMicroServicePinKey(), "Stav čerpadla"), ()->Main.getInput(WaterPumpFeedback.getMicroServicePinKey()).isHigh());
            WaterPumpFeedback.start(getInput(WaterPumpFeedback.getMicroServicePinKey()));
            ServiceMode.addInput(new NamedPort(RainSensor.getMicroServicePinKey(), "Dažďový senzor"), ()->Main.getInput(RainSensor.getMicroServicePinKey()).isHigh());
            RainSensor.start(getInput(RainSensor.getMicroServicePinKey()));
            WateringMain.main();
        }
        if (inputs.contains("--heating")) {
            HeatingWorker.start((key, value) -> {
                Main.getOutput(key).setState(value);
            });
        }
        if (inputs.contains("--solar")) {
            SolarMain.main(settingsDao);
        }

        webServer.join();
    }

    public static GpioPinDigitalInput getInput(String key) {
        String pinName = settingsDao.getString(key);
        GpioPinDigitalInput input = inputMap.get(pinName);
        if (input == null) {
            input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), key);
            inputMap.put(pinName, input);
        }
        return input;
    }

    public static GpioPinDigitalOutput getOutput(String key) {
        return getOutput(GpioFactory.getDefaultProvider(), RaspiPin.class, key);
    }

    public static GpioPinDigitalOutput getOutput(GpioProvider extendedProvider, Class<? extends PinProvider> pinClass, String key) {
        String pinName = settingsDao.getString(key);
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

    public static Map<String, GpioPinDigitalOutput> getOutputMap() {
        return outputMap;
    }

    public static Map<String, GpioPinDigitalInput> getInputMap() {
        return inputMap;
    }

}
