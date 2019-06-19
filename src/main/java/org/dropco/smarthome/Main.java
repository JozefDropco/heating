package org.dropco.smarthome;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
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
import org.dropco.smarthome.solar.SolarSystemDao;
import org.dropco.smarthome.solar.SolarSystemScheduler;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;
import org.dropco.smarthome.watering.WateringJob;
import org.dropco.smarthome.watering.WateringScheduler;
import org.dropco.smarthome.watering.WateringThreadManager;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.web.WebServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.dropco.smarthome.watering.WateringJob.WATERING_PUMP_STOP_DELAY;

public class Main {


    private static final SettingsDao settingsDao = new SettingsDao();
    private static final GpioController gpio = GpioFactory.getInstance();
    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception {
        // Create JAX-RS application.

        WebServer webServer = new WebServer();
        webServer.start();

        //heaterThread.start();
        webServer.join();
    }

}
