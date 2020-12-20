package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.*;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.solar.move.SafetySolarPanel;
import org.dropco.smarthome.solar.move.SolarPanelMover;
import org.dropco.smarthome.solar.move.SolarPanelThreadManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class SolarMain {

    private static final GpioController gpio = GpioFactory.getInstance();
    public static final String EXTEND_DATA_OUT_PIN = "EXTEND_DATA_OUT_PIN";
    public static final String EXTEND_CLOCK_PIN = "EXTEND_CLOCK_PIN";
    public static final String EXTEND_GATE_PIN = "EXTEND_GATE_PIN";
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    private static ExtendedGpioProvider extendedGpioProvider;

    public static void main(SettingsDao settingsDao) {
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.NORTH_PIN_REF_CD,"Kolektory - Sever"), key -> Main.getOutput(getExtendedProvider(),ExtendedPin.class,key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.SOUTH_PIN_REF_CD,"Kolektory - Juh"), key -> Main.getOutput(getExtendedProvider(),ExtendedPin.class,key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.EAST_PIN_REF_CD,"Kolektory - Východ"), key -> Main.getOutput(getExtendedProvider(),ExtendedPin.class,key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.WEST_PIN_REF_CD,"Kolektory - Západ"), key -> Main.getOutput(getExtendedProvider(),ExtendedPin.class,key));
        ServiceMode.getExclusions().put(SolarSystemRefCode.EAST_PIN_REF_CD,SolarSystemRefCode.WEST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.WEST_PIN_REF_CD,SolarSystemRefCode.EAST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.NORTH_PIN_REF_CD,SolarSystemRefCode.SOUTH_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.SOUTH_PIN_REF_CD,SolarSystemRefCode.NORTH_PIN_REF_CD);
        ServiceMode.addSubsriber(state-> {if (state) SolarPanelThreadManager.stop();});
        SolarSystemDao solarSystemDao = new SolarSystemDao(settingsDao);
        SolarPanelMover.setCommandExecutor((key, value) -> Main.getOutput(getExtendedProvider(),ExtendedPin.class,key).setState(value));
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



    static ExtendedGpioProvider getExtendedProvider() {
        if (extendedGpioProvider == null) {
            GpioPinDigitalOutput dataOutPin = Main.getOutput(EXTEND_DATA_OUT_PIN);
            GpioPinDigitalOutput clockPin = Main.getOutput(EXTEND_CLOCK_PIN);
            GpioPinDigitalOutput gatePin = Main.getOutput(EXTEND_GATE_PIN);
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

}
