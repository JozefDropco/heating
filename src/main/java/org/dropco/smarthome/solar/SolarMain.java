package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SolarMain {

    private static final GpioController gpio = GpioFactory.getInstance();
    public static final String EXTEND_DATA_OUT_PIN = "EXTEND_DATA_OUT_PIN";
    public static final String EXTEND_CLOCK_PIN = "EXTEND_CLOCK_PIN";
    public static final String EXTEND_GATE_PIN = "EXTEND_GATE_PIN";
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    private static ExtendedGpioProvider extendedGpioProvider;

    public static void main(SettingsDao settingsDao, Map<String, GpioPinDigitalOutput> outputMap, String[] args) {
        SolarSystemDao solarSystemDao = new SolarSystemDao(settingsDao);
        SolarPanelMover.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = outputMap.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(getExtendedProvider(settingsDao,outputMap), ExtendedPin.getPinByName(pinName), key, PinState.LOW);
                outputMap.put(pinName, output);
            }
            output.setState(value);
        });
        SolarPanelMover.setCurrentPositionSupplier(() -> solarSystemDao.getLastKnownPosition());
        SolarPanelMover.addListener(panel -> solarSystemDao.updateLastKnownPosition(panel));
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean solarOverHeated = new AtomicBoolean(false);
        SafetySolarPanel safetySolarPanel = new SafetySolarPanel(solarOverHeated, strongWind, () -> solarSystemDao.getOverheatedPosition(), () -> solarSystemDao.getStrongWindPosition());
        overHeatedHandler(solarOverHeated, safetySolarPanel);
        startStrongWindDetector(settingsDao,solarOverHeated, safetySolarPanel);
        SolarSystemScheduler solarSystemScheduler = new SolarSystemScheduler(solarSystemDao);
        solarSystemScheduler.moveToLastPositioon(safetySolarPanel);
        solarSystemScheduler.schedule(safetySolarPanel);

    }



    static ExtendedGpioProvider getExtendedProvider(SettingsDao settingsDao, Map<String, GpioPinDigitalOutput> outputMap) {
        if (extendedGpioProvider == null) {
            GpioPinDigitalOutput dataOutPin = getRaspiPin(settingsDao,outputMap,EXTEND_DATA_OUT_PIN);
            GpioPinDigitalOutput clockPin = getRaspiPin(settingsDao,outputMap,EXTEND_CLOCK_PIN);
            GpioPinDigitalOutput gatePin = getRaspiPin(settingsDao,outputMap,EXTEND_GATE_PIN);
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

    static void startStrongWindDetector(SettingsDao settingsDao,AtomicBoolean strongWind, SafetySolarPanel safetySolarPanel) {
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


    private static GpioPinDigitalOutput getRaspiPin(SettingsDao settingsDao, Map<String, GpioPinDigitalOutput> outputMap, String pin) {
        String pinName = settingsDao.getString(pin);
        GpioPinDigitalOutput output = outputMap.get(pinName);
        if (output == null) {
            output = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), pin, PinState.LOW);
            outputMap.put(pinName, output);
        }
        return output;
    }
}
