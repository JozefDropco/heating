package org.dropco.smarthome;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.PinListener;
import com.pi4j.io.gpio.impl.GpioControllerImpl;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainTest {
   private static final Map<Pin, PinState> stateMap= Collections.synchronizedMap(Maps.newHashMap());
   private static final List<PinListener> northSouthList= Lists.newArrayList();
   private static final List<PinListener> eastWestList= Lists.newArrayList();
   private static final TickerPin nstickerPin= new TickerPin(250, 10000000);
   private static final TickerPin ewtickerPin= new TickerPin(250, 10000000);
    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
        simulate();
    }

    public static void simulate() throws NoSuchFieldException, IllegalAccessException {
        Field instance = GpioFactory.class.getDeclaredField("controller");
        instance.setAccessible(true);
        instance.set(null,new GpioControllerImpl(new GpioProvider() {
            @Override
            public String getName() {
                return "RaspberryPi GPIO Provider";
            }

            @Override
            public boolean hasPin(Pin pin) {
                return false;
            }

            @Override
            public void export(Pin pin, PinMode mode, PinState defaultState) {

            }

            @Override
            public void export(Pin pin, PinMode mode) {

            }

            @Override
            public boolean isExported(Pin pin) {
                return false;
            }

            @Override
            public void unexport(Pin pin) {

            }

            @Override
            public void setMode(Pin pin, PinMode mode) {

            }

            @Override
            public PinMode getMode(Pin pin) {
                return null;
            }

            @Override
            public void setPullResistance(Pin pin, PinPullResistance resistance) {

            }

            @Override
            public PinPullResistance getPullResistance(Pin pin) {
                return null;
            }

            @Override
            public void setState(Pin pin, PinState state) {

            }

            @Override
            public PinState getState(Pin pin) {
                return null;
            }

            @Override
            public void setValue(Pin pin, double value) {

            }

            @Override
            public double getValue(Pin pin) {
                return 0;
            }

            @Override
            public void setPwm(Pin pin, int value) {

            }

            @Override
            public void setPwmRange(Pin pin, int range) {

            }

            @Override
            public int getPwm(Pin pin) {
                return 0;
            }

            @Override
            public void addListener(Pin pin, PinListener listener) {
                if (pin.getName().equals("GPIO 11")) ewtickerPin.addListener(listener);
                if (pin.getName().equals("GPIO 10")) nstickerPin.addListener(listener);
            }

            @Override
            public void removeListener(Pin pin, PinListener listener) {

            }

            @Override
            public void removeAllListeners() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public boolean isShutdown() {
                return false;
            }
        }));
        instance = GpioFactory.class.getDeclaredField("provider");
        instance.setAccessible(true);
        instance.set(null,new GpioProvider(){
            @Override
            public String getName() {
                return "RaspberryPi GPIO Provider";
            }

            @Override
            public boolean hasPin(Pin pin) {
                return false;
            }

            @Override
            public void export(Pin pin, PinMode pinMode, PinState pinState) {

            }

            @Override
            public void export(Pin pin, PinMode pinMode) {

            }

            @Override
            public boolean isExported(Pin pin) {
                return false;
            }

            @Override
            public void unexport(Pin pin) {

            }

            @Override
            public void setMode(Pin pin, PinMode pinMode) {

            }

            @Override
            public PinMode getMode(Pin pin) {
                return null;
            }

            @Override
            public void setPullResistance(Pin pin, PinPullResistance pinPullResistance) {

            }

            @Override
            public PinPullResistance getPullResistance(Pin pin) {
                return null;
            }

            @Override
            public void setState(Pin pin, PinState pinState) {
                stateMap.put(pin,pinState);
                String name = pin.getName();
                switch (name){
                    case "GPIO 0":
                    case "GPIO 30":
                        if (pinState.isHigh()){
                            nstickerPin.startTicking();
                        } else{
                            nstickerPin.stopTicking();
                        }
                        break;
                    case "GPIO 2":
                    case "GPIO 3":
                        if (pinState.isHigh()){
                            ewtickerPin.startTicking();
                        } else{
                            ewtickerPin.stopTicking();
                        }
                        break;
                    default:
                }

            }

            @Override
            public PinState getState(Pin pin) {
              return stateMap.getOrDefault(pin,PinState.LOW);
            }

            @Override
            public void setValue(Pin pin, double v) {

            }

            @Override
            public double getValue(Pin pin) {
                return 0;
            }

            @Override
            public void setPwm(Pin pin, int i) {

            }

            @Override
            public void setPwmRange(Pin pin, int i) {

            }

            @Override
            public int getPwm(Pin pin) {
                return 0;
            }

            @Override
            public void addListener(Pin pin, PinListener pinListener) {
                if (pin.getName().equals("GPIO 11")) ewtickerPin.addListener((GpioPinListener) pinListener);
                if (pin.getName().equals("GPIO 10")) nstickerPin.addListener((GpioPinListener) pinListener);
            }

            @Override
            public void removeListener(Pin pin, PinListener pinListener) {

            }

            @Override
            public void removeAllListeners() {

            }

            @Override
            public void shutdown() {

            }

            @Override
            public boolean isShutdown() {
                return false;
            }
        });
    }

    @Test
    public void run() throws Exception {
        Logger.getGlobal().setLevel(Level.FINE);
        System.setProperty("html","C:\\SRDEV\\projects\\heating\\resources");
        Main.main(new String[]{"--heating","--solar"});
    }
}
