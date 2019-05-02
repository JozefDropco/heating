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
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.web.WebServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Main {


    private static final SettingsDao settingsDao = new SettingsDao();
    private static final SolarSystemDao solarSystemDao = new SolarSystemDao(settingsDao);
    private static final GpioController gpio = GpioFactory.getInstance();
    public static final String EXTEND_DATA_OUT_PIN = "EXTEND_DATA_OUT_PIN";
    public static final String EXTEND_CLOCK_PIN = "EXTEND_CLOCK_PIN";
    public static final String EXTEND_GATE_PIN = "EXTEND_GATE_PIN";
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    private static ExtendedGpioProvider extendedGpioProvider;
    private static Map<String, GpioPinDigitalOutput> outputMap = Collections.synchronizedMap(new HashMap<>());
    private static Map<String, GpioPinDigitalInput> inputMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) throws Exception {
        // Create JAX-RS application.

        WebServer webServer = new WebServer();
        webServer.start();
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean solarOverHeated = new AtomicBoolean(false);
        SolarPanelMover.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = outputMap.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(getExtendedProvider(), ExtendedPin.getPinByName(pinName), key, PinState.LOW);
                outputMap.put(pinName, output);
            }
            output.setState(value);
        });
        SolarPanelMover.setCurrentPositionSupplier(() -> solarSystemDao.getLastKnownPosition());
        SolarPanelMover.addListener(panel -> solarSystemDao.updateLastKnownPosition(panel));
        SafetySolarPanel safetySolarPanel = new SafetySolarPanel(solarOverHeated, strongWind, () -> solarSystemDao.getOverheatedPosition(), () -> solarSystemDao.getStrongWindPosition());
        overHeatedHandler(solarOverHeated, safetySolarPanel);
        startStrongWindDetector(solarOverHeated, safetySolarPanel);
        SolarSystemScheduler solarSystemScheduler = new SolarSystemScheduler(solarSystemDao);
        solarSystemScheduler.moveToLastPositioon(safetySolarPanel);
        solarSystemScheduler.schedule(safetySolarPanel);
        Thread heaterThread = new Thread(new HeatingWorker(value -> solarOverHeated.set(value), settingsDao));
        WateringJob.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = outputMap.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), key, PinState.LOW);
                outputMap.put(pinName, output);
            }
            output.setState(value);
        });
        WateringJob.setZones(new WateringDao()::getActiveZones);
        WateringJob.setTemperatureThreshold(() -> settingsDao.getDouble(WateringJob.TEMP_THRESHOLD));
        WateringJob.setRaining(()->{
            String pinName = settingsDao.getString(WateringJob.RAIN_SENSOR);
            GpioPinDigitalInput input = inputMap.get(pinName);
            if (input == null) {
                input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), WateringJob.RAIN_SENSOR);
                inputMap.put(pinName, input);
            }
            return input.getState()==PinState.LOW;
        });
        WateringJob.setTemperature(() -> {
            W1Master master = new W1Master();
            List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
            String deviceId = new HeatingDao().getDeviceId(HeatingRefCode.EXTERNAL_TEMPERATURE_PLACE_REF_CD);
            Optional<TemperatureSensor> externalTemp = FluentIterable.from(sensors).filter(sensor -> ((W1Device) sensor).getId().trim().equals(deviceId)).first();
            if (externalTemp.isPresent()) {
                return externalTemp.get().getTemperature(TemperatureScale.CELSIUS);
            }
            return -10.0;
        });
        WateringJob.setWatchPumpSupplier(thread -> {
            String pinName = settingsDao.getString(WateringJob.WATER_PUMP_REF_CD);
            GpioPinDigitalInput input = inputMap.get(pinName);
            if (input == null) {
                input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), WateringJob.WATER_PUMP_REF_CD);
                inputMap.put(pinName, input);
            }
            AtomicBoolean wasActive = new AtomicBoolean(input.getState() == PinState.HIGH);
            input.addListener((GpioPinListenerDigital) event -> {
                if (event.getState() == PinState.HIGH) wasActive.set(true);
            });
            GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> {
                if (!wasActive.get()) {
                    thread.interrupt();
                }
            }, settingsDao.getLong(WateringJob.WATER_PUMP_WAIT_TIME), TimeUnit.MILLISECONDS);
        });
        new WateringScheduler(new WateringDao()).schedule();
        heaterThread.start();
        webServer.join();
    }

    static ExtendedGpioProvider getExtendedProvider() {
        if (extendedGpioProvider == null) {
            GpioPinDigitalOutput dataOutPin = getRaspiPin(EXTEND_DATA_OUT_PIN);
            GpioPinDigitalOutput clockPin = getRaspiPin(EXTEND_CLOCK_PIN);
            GpioPinDigitalOutput gatePin = getRaspiPin(EXTEND_GATE_PIN);
            extendedGpioProvider = new ExtendedGpioProvider(gpio, dataOutPin, clockPin, gatePin);
        }
        //reload if needed
        return extendedGpioProvider;
    }

    static Consumer<Boolean> overHeatedHandler(AtomicBoolean solarOverHeated, SafetySolarPanel safetySolarPanel) {
        return overHeated -> {
            solarOverHeated.set(overHeated);
            if (overHeated)
                safetySolarPanel.moveToOverheatedPosition();
            else
                safetySolarPanel.backToNormal();
        };
    }

    static void startStrongWindDetector(AtomicBoolean strongWind, SafetySolarPanel safetySolarPanel) {
        GpioPinDigitalOutput strongWindPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(settingsDao.getString(STRONG_WIND_PIN_REF_CD)), STRONG_WIND_PIN_REF_CD, PinState.LOW);
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.HIGH, 5000, strongWindPin) {
            @Override
            public void handleStateChange(boolean state) {
                strongWind.set(state);
                if (state) {
                    safetySolarPanel.moveToStrongWindPosition();
                } else {
                    safetySolarPanel.backToNormal();
                }
            }
        });
    }

    private static GpioPinDigitalOutput getRaspiPin(String pin) {
        String pinName = settingsDao.getString(pin);
        GpioPinDigitalOutput output = outputMap.get(pinName);
        if (output == null) {
            output = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), pin, PinState.LOW);
            outputMap.put(pinName, output);
        } return output;
    }
}
