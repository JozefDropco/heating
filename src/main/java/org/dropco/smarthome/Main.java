package org.dropco.smarthome;

import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.SolarSystemDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.heating.HeatingWorker;
import org.dropco.smarthome.solar.SolarSystemScheduler;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;

import java.util.HashMap;
import java.util.Map;
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
    private static Map<String, GpioPinDigitalOutput> map = new HashMap<>();

    public static void main(String[] args) {
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean solarOverHeated = new AtomicBoolean(false);
        SolarPanelMover.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = map.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(getExtendedProvider(), ExtendedPin.getPinByName(pinName), key, PinState.LOW);
                map.put(pinName, output);
            }
            output.setState(value);
        });
        SolarPanelMover.setCurrentPositionSupplier(() -> solarSystemDao.getLastKnownPosition());
        SolarPanelMover.addListener(panel -> solarSystemDao.updateLastKnownPosition(panel));
        SafetySolarPanel safetySolarPanel = new SafetySolarPanel(solarOverHeated, strongWind, () -> solarSystemDao.getOverheatedPosition(), () -> solarSystemDao.getStrongWindPosition());
        overHeatedHandler(solarOverHeated, safetySolarPanel);
        startStrongWindDetector(solarOverHeated, safetySolarPanel);
        new SolarSystemScheduler(solarSystemDao).schedule(safetySolarPanel);
        Thread heaterThread = new Thread(new HeatingWorker(value -> solarOverHeated.set(value), settingsDao));
        heaterThread.start();
        try {
            synchronized (Thread.currentThread()) {
                Thread.currentThread().wait();
            }
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
        strongWindPin.addListener(new DelayedGpioPinListener(PinState.HIGH,5000,strongWindPin) {
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
}
