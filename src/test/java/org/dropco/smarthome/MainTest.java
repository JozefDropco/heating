package org.dropco.smarthome;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.PinListener;
import com.pi4j.io.gpio.impl.GpioControllerImpl;
import org.dropco.smarthome.gpioextension.ExtendedGpioProvider;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

public class MainTest {

    @Before
    public void init() throws NoSuchFieldException, IllegalAccessException {
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

            }

            @Override
            public PinState getState(Pin pin) {
                return PinState.LOW;
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
        ExtendedGpioProvider.simulate();
    }

    @Test
    public void run() throws Exception {
        Main.main(new String[]{"--solar","--heating"});
    }
}
