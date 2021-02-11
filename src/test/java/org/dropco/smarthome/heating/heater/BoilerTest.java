package org.dropco.smarthome.heating.heater;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioTrigger;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BoilerTest {

    @Test
    public void test() throws InterruptedException {
        Boiler b = new Boiler(MOCKED_PIN);
        b.start(3000);
        System.out.println(Boiler.getState() + "mockED PIN:"+MOCKED_PIN.getState());
        Thread.sleep(1000);
        MOCKED_PIN.flipState();
        System.out.println(Boiler.getState() + "mockED PIN:"+MOCKED_PIN.getState());
        Thread.sleep(1000);
        MOCKED_PIN.flipState();
        System.out.println(Boiler.getState() + "mockED PIN:"+MOCKED_PIN.getState());

        Thread.sleep(5000);
        MOCKED_PIN.flipState();
        System.out.println(Boiler.getState() + "mockED PIN:"+MOCKED_PIN.getState());
        Thread.sleep(5000);
        System.out.println(Boiler.getState() + "mockED PIN:"+MOCKED_PIN.getState());

    }

    private static final MockedInput MOCKED_PIN = new MockedInput();


    private static class MockedInput implements GpioPinDigitalInput {
        private static final AtomicBoolean state = new AtomicBoolean(false);
        private static final List<GpioPinListener> list = Lists.newArrayList();

        public void flipState() {
            state.set(!state.get());
            list.forEach(gpioPinListener ->
                    ((GpioPinListenerDigital) gpioPinListener)
                            .handleGpioPinDigitalStateChangeEvent(new GpioPinDigitalStateChangeEvent(new Object(), MOCKED_PIN, getState())));
        }

        @Override
        public Collection<GpioTrigger> getTriggers() {
            return null;
        }

        @Override
        public void addTrigger(GpioTrigger... gpioTriggers) {

        }

        @Override
        public void addTrigger(List<? extends GpioTrigger> list) {

        }

        @Override
        public void removeTrigger(GpioTrigger... gpioTriggers) {

        }

        @Override
        public void removeTrigger(List<? extends GpioTrigger> list) {

        }

        @Override
        public void removeAllTriggers() {

        }

        @Override
        public boolean hasDebounce(PinState pinState) {
            return false;
        }

        @Override
        public int getDebounce(PinState pinState) {
            return 0;
        }

        @Override
        public void setDebounce(int i) {

        }

        @Override
        public void setDebounce(int i, PinState... pinStates) {

        }

        @Override
        public boolean isHigh() {
            return state.get();
        }

        @Override
        public boolean isLow() {
            return !state.get();
        }

        @Override
        public PinState getState() {
            return state.get() ? PinState.HIGH : PinState.LOW;
        }

        @Override
        public boolean isState(PinState pinState) {
            return pinState == PinState.HIGH && state.get();
        }

        @Override
        public GpioProvider getProvider() {
            return null;
        }

        @Override
        public Pin getPin() {
            return null;
        }

        @Override
        public void setName(String s) {

        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public void setTag(Object o) {

        }

        @Override
        public Object getTag() {
            return null;
        }

        @Override
        public void setProperty(String s, String s1) {

        }

        @Override
        public boolean hasProperty(String s) {
            return false;
        }

        @Override
        public String getProperty(String s) {
            return null;
        }

        @Override
        public String getProperty(String s, String s1) {
            return null;
        }

        @Override
        public Map<String, String> getProperties() {
            return null;
        }

        @Override
        public void removeProperty(String s) {

        }

        @Override
        public void clearProperties() {

        }

        @Override
        public void export(PinMode pinMode) {

        }

        @Override
        public void export(PinMode pinMode, PinState pinState) {

        }

        @Override
        public void unexport() {

        }

        @Override
        public boolean isExported() {
            return false;
        }

        @Override
        public void setMode(PinMode pinMode) {

        }

        @Override
        public PinMode getMode() {
            return null;
        }

        @Override
        public boolean isMode(PinMode pinMode) {
            return false;
        }

        @Override
        public void setPullResistance(PinPullResistance pinPullResistance) {

        }

        @Override
        public PinPullResistance getPullResistance() {
            return null;
        }

        @Override
        public boolean isPullResistance(PinPullResistance pinPullResistance) {
            return false;
        }

        @Override
        public Collection<GpioPinListener> getListeners() {
            return null;
        }

        @Override
        public void addListener(GpioPinListener... gpioPinListeners) {
            list.addAll(Arrays.asList(gpioPinListeners));
        }

        @Override
        public void addListener(List<? extends GpioPinListener> list) {

        }

        @Override
        public boolean hasListener(GpioPinListener... gpioPinListeners) {
            return false;
        }

        @Override
        public void removeListener(GpioPinListener... gpioPinListeners) {

        }

        @Override
        public void removeListener(List<? extends GpioPinListener> list) {

        }

        @Override
        public void removeAllListeners() {

        }

        @Override
        public GpioPinShutdown getShutdownOptions() {
            return null;
        }

        @Override
        public void setShutdownOptions(GpioPinShutdown gpioPinShutdown) {

        }

        @Override
        public void setShutdownOptions(Boolean aBoolean) {

        }

        @Override
        public void setShutdownOptions(Boolean aBoolean, PinState pinState) {

        }

        @Override
        public void setShutdownOptions(Boolean aBoolean, PinState pinState, PinPullResistance pinPullResistance) {

        }

        @Override
        public void setShutdownOptions(Boolean aBoolean, PinState pinState, PinPullResistance pinPullResistance, PinMode pinMode) {

        }
    }

    ;

}
