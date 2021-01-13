package org.dropco.smarthome.solar;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DayLight {
    private static DayLight instance;
    private final AtomicBoolean enoughLight = new AtomicBoolean(false);
    private final List<Consumer<Boolean>> subscribers = Lists.newArrayList();
    private final GpioPinDigitalInput input;
    private final Supplier<Integer> lightThreshold;
    private DelayedGpioPinListener pinListener;

    private DayLight(GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        this.input = input;
        this.lightThreshold = lightThreshold;
    }

    /***
     * Gets the instance
     * @return
     */
    public static void setInstance(GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        instance = new DayLight(input, lightThreshold);
    }

    /***
     * Gets the instance
     * @return
     */
    public static DayLight inst() {
        return instance;
    }

    public void connect(boolean initialValue) {
        enoughLight.set(initialValue);
        StatsCollector.getInstance().collect("Jas",input);
        pinListener = new DelayedGpioPinListener(PinState.HIGH, lightThreshold.get(), input) {


            @Override
            public void handleStateChange(boolean state) {
                boolean success = enoughLight.compareAndSet(false, state);
                if (success) {
                    new SettingsDao().setLong(SolarSystemRefCode.DAYLIGHT,1);
                    subscribers.forEach(booleanConsumer -> booleanConsumer.accept(true));
                }
            }
        };
        input.addListener(pinListener);

    }

    /***
     * Gets the maxContinuousLight
     * @return
     */
    public  boolean enoughLight() {
        return enoughLight.get();
    }

    public void clear() {
        enoughLight.set(false);
        new SettingsDao().setLong(SolarSystemRefCode.DAYLIGHT,0);
        if (input.isHigh()){
            pinListener.delayedCheck();
        }
    }

    public void subscribe(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }
}
