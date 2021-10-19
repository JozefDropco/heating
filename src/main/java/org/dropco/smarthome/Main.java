package org.dropco.smarthome;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.HeatingMain;
import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.heating.solar.SolarMain;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.temp.TempService;
import org.dropco.smarthome.watering.WateringMain;
import org.dropco.smarthome.web.WebServer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main {


    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());

    private static GpioController gpio = GpioFactory.getInstance();
    public static final Set<String> INPUTS = Sets.newHashSet();

    public static void main(String[] args) throws Exception {
        Db.acceptDao(new SettingsDao(), settingsDao -> {
            // Create JAX-RS application.
            new Thread(new TempService()).start();
            StatsCollector.getInstance().start(settingsDao);
            INPUTS.addAll(Arrays.asList(args));
            if (!INPUTS.contains("--noWatering")) {
                ServiceMode.addInput(new NamedPort(WaterPumpFeedback.getMicroServicePinKey(), "Stav čerpadla"), () -> Main.getInput(WaterPumpFeedback.getMicroServicePinKey()).getState() == WaterPumpFeedback.LOGICAL_HIGH_STATE);
                WaterPumpFeedback.start(getInput(WaterPumpFeedback.getMicroServicePinKey()));
                ServiceMode.addInput(new NamedPort(RainSensor.getMicroServicePinKey(), "Dažďový senzor"), () -> Main.getInput(RainSensor.getMicroServicePinKey()).getState() == RainSensor.RAIN_STATE);
                RainSensor.start(getInput(RainSensor.getMicroServicePinKey()));
                WateringMain.main(settingsDao);
            }
            if (INPUTS.contains("--heating")) {
                HeatingMain.start(settingsDao);
            }
            if (INPUTS.contains("--solar")) {
            }

        });
        WebServer webServer = new WebServer();
        webServer.start();
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.ALL);
        LogHandler handler = new LogHandler();
        logger.addHandler(handler);
        handler.setLevel(Level.ALL);
        webServer.join();
    }

    public static GpioPinDigitalInput getInput(String key) {
        String pinName = Db.applyDao(new SettingsDao(), dao -> dao.getString(key));
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

    public static Map<String, GpioPinDigitalOutput> getOutputMap() {
        return outputMap;
    }

    public static Map<String, GpioPinDigitalInput> getInputMap() {
        return inputMap;
    }

}
