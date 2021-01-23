package org.dropco.smarthome.solar;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DayLight {
    private static DayLight instance;
    private final AtomicBoolean enoughLight = new AtomicBoolean(false);
    private final List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    private final GpioPinDigitalInput input;
    private final Supplier<Integer> lightThreshold;
    private DelayedGpioPinListener pinListener;
    private SettingsDao settingsDao;
    private static final Logger LOGGER = Logger.getLogger(DayLight.class.getName());

    private DayLight(SettingsDao settingsDao, GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        this.settingsDao = settingsDao;
        this.input = input;
        this.lightThreshold = lightThreshold;
    }

    /***
     * Gets the instance
     * @return
     */
    public static void setInstance(SettingsDao settingsDao, GpioPinDigitalInput input, Supplier<Integer> lightThreshold) {
        instance = new DayLight(settingsDao, input, lightThreshold);
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
        StatsCollector.getInstance().collect("Jas", input,PinState.LOW);
        pinListener = new DelayedGpioPinListener(PinState.LOW, lightThreshold.get(), input) {


            @Override
            public void handleStateChange(boolean state) {
                boolean success = enoughLight.compareAndSet(false, state);
                if (success) {
                    settingsDao.setLong(SolarSystemRefCode.DAYLIGHT, 1);
                    LOGGER.log(Level.INFO,"Denný jas splnený");
                    subscribers.forEach(booleanConsumer -> booleanConsumer.accept(true));
                }
            }
        };
        input.setPullResistance(PinPullResistance.PULL_UP);
        input.addListener(pinListener);

    }

    /***
     * Gets the maxContinuousLight
     * @return
     */
    public boolean enoughLight() {
        return enoughLight.get();
    }

    public void clear() {
        enoughLight.set(false);
        LOGGER.log(Level.INFO, "Reset denného jasu");
        settingsDao.setLong(SolarSystemRefCode.DAYLIGHT, 0);
        if (input.isLow()) {
            pinListener.delayedCheck();
        }
    }

    public void subscribe(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }

    public boolean getCurrentState() {
        return input.isLow();
    }
}
