package org.dropco.smarthome;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.heating.HeatingDao;
import org.dropco.smarthome.heating.HeatingRefCode;
import org.dropco.smarthome.heating.HeatingWorker;
import org.dropco.smarthome.solar.SolarMain;
import org.dropco.smarthome.solar.SolarSystemDao;
import org.dropco.smarthome.solar.SolarSystemScheduler;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;
import org.dropco.smarthome.watering.WateringJob;
import org.dropco.smarthome.watering.WateringMain;
import org.dropco.smarthome.watering.WateringScheduler;
import org.dropco.smarthome.watering.WateringThreadManager;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.web.WebServer;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.dropco.smarthome.watering.WateringJob.WATERING_PUMP_STOP_DELAY;

public class Main {


    private static final SettingsDao settingsDao = new SettingsDao();
    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());


    public static void main(String[] args) throws Exception {
        // Create JAX-RS application.

        WebServer webServer = new WebServer();
        webServer.start();
        Set<String> inputs = Sets.newHashSet(args);
        if (!inputs.contains("--noWatering")){
            WateringMain.main(settingsDao,inputMap,outputMap,args);
        }

        if (!inputs.contains("--noSolar")){
            SolarMain.main(settingsDao,outputMap,args);
        }
        webServer.join();
    }

}
