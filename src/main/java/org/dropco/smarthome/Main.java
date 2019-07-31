package org.dropco.smarthome;

import com.google.common.collect.Sets;
import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.HeatingDao;
import org.dropco.smarthome.heating.HeatingRefCode;
import org.dropco.smarthome.microservice.OutsideTemperature;
import org.dropco.smarthome.microservice.RainSensor;
import org.dropco.smarthome.microservice.WaterPumpFeedback;
import org.dropco.smarthome.solar.SolarMain;
import org.dropco.smarthome.watering.WateringMain;
import org.dropco.smarthome.web.WebServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class Main {


    private static final SettingsDao settingsDao = new SettingsDao();
    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());

    private static GpioController gpio = GpioFactory.getInstance();

    public static void main(String[] args) throws Exception {
        // Create JAX-RS application.

        WebServer webServer = new WebServer();
        webServer.start();
        Set<String> inputs = Sets.newHashSet(args);
        if (!inputs.contains("--noWatering")) {
            WaterPumpFeedback.start(gpio.provisionDigitalInputPin(RaspiPin.getPinByName(settingsDao.getString(WaterPumpFeedback.getMicroServicePinKey())), WaterPumpFeedback.getMicroServicePinKey()));
            RainSensor.start(gpio.provisionDigitalInputPin(RaspiPin.getPinByName(settingsDao.getString(RainSensor.getMicroServicePinKey())), RainSensor.getMicroServicePinKey()));
            OutsideTemperature.start(new HeatingDao().getDeviceId(HeatingRefCode.EXTERNAL_TEMPERATURE_PLACE_REF_CD));
            WateringMain.main(settingsDao, inputMap, outputMap, args);
        }
        if (!inputs.contains("--noSolar")) {
            SolarMain.main(settingsDao, outputMap, args);
        }
        webServer.join();
    }

}
