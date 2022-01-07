package org.dropco.smarthome;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.trigger.GpioTrigger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TickerPin implements GpioPinDigitalInput {
    private List<GpioPinListenerDigital> listeners = Lists.newArrayList();
    private int sleepMiliseconds;
    private final int tickCount;
    private AtomicBoolean executed = new AtomicBoolean();
    private ReentrantLock lock = new ReentrantLock();
    private Condition waitForResume = lock.newCondition();
    private AtomicBoolean stopped = new AtomicBoolean();

    public TickerPin(int sleepMiliseconds, int tickCount) {
        this.sleepMiliseconds = sleepMiliseconds;
        this.tickCount = tickCount;
    }

    public void startTicking() {
        if (executed.compareAndSet(false, true))
            Executors.defaultThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    int tickCount = TickerPin.this.tickCount;
                    while (tickCount > 0) {
                        if (stopped.get()) {
                            lock.lock();
                            try{
                                waitForResume.awaitUninterruptibly();
                            }finally {
                                lock.unlock();
                            }
                        }
                        tickCount--;
                        Logger.getLogger(TickerPin.class.getName()).log(Level.INFO,"Tick");
                        listeners.forEach(gpioPinListener -> gpioPinListener.handleGpioPinDigitalStateChangeEvent(new GpioPinDigitalStateChangeEvent(TickerPin.this, TickerPin.this, PinState.HIGH)));
                        try {
                            Thread.sleep(sleepMiliseconds);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        listeners.forEach(gpioPinListener -> gpioPinListener.handleGpioPinDigitalStateChangeEvent(new GpioPinDigitalStateChangeEvent(TickerPin.this, TickerPin.this, PinState.LOW)));

                    }
                }
            }).start();
        else {
            lock.lock();
            try{
                stopped.set(false);
                waitForResume.signal();
            } finally {
                lock.unlock();
            }
        }
    }

    public void stopTicking() {
        stopped.set(true);
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
        return false;
    }

    @Override
    public boolean isLow() {
        return false;
    }

    @Override
    public PinState getState() {
        return null;
    }

    @Override
    public boolean isState(PinState pinState) {
        return false;
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
        for (GpioPinListener listener : gpioPinListeners)
            listeners.add((GpioPinListenerDigital) listener);
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
