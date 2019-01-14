package org.dropco.smarthome;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.SolarSystemDao;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.solar.PositionUpdater;
import org.dropco.smarthome.solar.SolarPanel;
import org.dropco.smarthome.solar.SolarSystemWorker;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Main {


    private static final AtomicBoolean shutdownRequested = new AtomicBoolean(false);
    private static final SettingsDao settingsDao = new SettingsDao();
    private static final SolarSystemDao solarSystemDao = new SolarSystemDao(settingsDao);
    private static final GpioController gpio = GpioFactory.getInstance();
    public static final String EXTEND_DATA_OUT_PIN = "EXTEND_DATA_OUT_PIN";
    public static final String EXTEND_CLOCK_PIN = "EXTEND_CLOCK_PIN";
    public static final String EXTEND_GATE_PIN = "EXTEND_GATE_PIN";
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    private static ExtendedGpioProvider extendedGpioProvider;
    private static Map<String, GpioPinDigitalOutput> map = new HashMap<>();

    public static void main(String[] args) {
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean solarOverHeated = new AtomicBoolean(false);
        SolarPanel solarPanel = new SolarPanel(solarSystemDao.getLastKnownPosition(), (key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = map.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(getExtendedProvider(), ExtendedPin.getPinByName(pinName), key, PinState.LOW);
                map.put(pinName, output);
            }
            output.setState(value);
        });
        PositionUpdater positionUpdater = new PositionUpdater(solarSystemDao);
        solarPanel.addListener(panel -> positionUpdater.add(panel.getCurrentPosition()));
        Thread solarMovementThread = new Thread(new SolarSystemWorker(shutdownRequested, strongWind, solarOverHeated, solarSystemDao, solarPanel));
        solarMovementThread.start();
        Thread positionUpdaterThread = new Thread(positionUpdater);
        positionUpdaterThread.start();
//        startStrongWindDetector(strongWind, solarMovementThread);
//        Thread heatingThread = new Thread(new HeatingWorker(shutdownRequested, overHeatedHandler(solarOverHeated, solarMovementThread),settingsDao,new HeatingDao()));
//        heatingThread.start();
        try {
            solarMovementThread.join();
            positionUpdaterThread.join();
//            heatingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static ExtendedGpioProvider getExtendedProvider() {
        if (extendedGpioProvider == null) {
            GpioPinDigitalOutput dataOutPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(settingsDao.getString(EXTEND_DATA_OUT_PIN)), EXTEND_DATA_OUT_PIN, PinState.LOW);
            GpioPinDigitalOutput clockPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(settingsDao.getString(EXTEND_CLOCK_PIN)), EXTEND_CLOCK_PIN, PinState.LOW);
            GpioPinDigitalOutput gatePin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(settingsDao.getString(EXTEND_GATE_PIN)), EXTEND_GATE_PIN, PinState.LOW);
            extendedGpioProvider = new ExtendedGpioProvider(gpio, dataOutPin, clockPin, gatePin);
        }
        //reload if needed
        return extendedGpioProvider;
    }

    static Consumer<Boolean> overHeatedHandler(AtomicBoolean solarOverHeated, Thread solarMovementThread) {
        return overHeated -> {
            solarOverHeated.set(overHeated);
            if (overHeated) {
                synchronized (solarMovementThread) {
                    solarMovementThread.notify();
                }
            }
        };
    }

    static void startStrongWindDetector(AtomicBoolean strongWind, Thread solarThread) {
        GpioPinDigitalOutput strongWindPin = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(settingsDao.getString(STRONG_WIND_PIN_REF_CD)), STRONG_WIND_PIN_REF_CD, PinState.LOW);
        strongWindPin.addListener((GpioPinListenerDigital) event -> {
            boolean value = event.getState() == PinState.HIGH;
            strongWind.set(value);
            if (value) {
                synchronized (solarThread) {
                    solarThread.notify();
                }
            }
        });
    }
}
