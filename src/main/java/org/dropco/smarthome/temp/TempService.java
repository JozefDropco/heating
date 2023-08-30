package org.dropco.smarthome.temp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.AtomicDouble;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.db.MeasurePlace;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempService implements Runnable {
    private static final Map<String, AtomicDouble> recentTemperatures = new HashMap<>();
    private static final Map<String, List<Consumer<Double>>> subscribers = Maps.newHashMap();
    protected static final double INITIAL_VALUE = -999;


    @Override
    public void run() {
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                W1Master master = new W1Master();
                List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
                for (TemperatureSensor sensor : sensors) {
                    W1Device device = (W1Device) sensor;
                    double temperature = sensor.getTemperature(TemperatureScale.CELSIUS);
                    if (!(Double.compare(temperature, 85.0d) == 0 || temperature < -55.0 || temperature > 125.0)) {
                        String deviceId = device.getId().trim();
                        Optional<MeasurePlace> tempSensor = Db.applyDao(new HeatingDao(), dao -> dao.getById(deviceId));
                        if (tempSensor.isPresent()) {
                            temperature += tempSensor.get().getAdjustmentTemp();
                        }
                        update(deviceId, temperature, tempSensor);
                    }
                }
            } catch (RuntimeException e) {
                Logger.getLogger(TempService.class.getName()).log(Level.FINE, "Temp service not working", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private static void update(String deviceId, double temperature, Optional<MeasurePlace> tempSensor) {
        AtomicDouble value = recentTemperatures.computeIfAbsent(deviceId, key -> new AtomicDouble(INITIAL_VALUE));
        double oldValue = value.getAndSet(temperature);
        if (Double.compare(oldValue, temperature) != 0 && Double.compare(INITIAL_VALUE, oldValue) != 0) {
            Db.acceptDao(new LogDao(), log -> log.logTemperature(deviceId, tempSensor.map(MeasurePlace::getRefCd).orElse(null), new Date(), temperature));
            subscribers.computeIfAbsent(deviceId, key -> Lists.newArrayList()).forEach(subscriber -> subscriber.accept(temperature));
        }
    }


    public static void subscribe(String deviceId, Consumer<Double> subscriber) {
        subscribers.computeIfAbsent(deviceId, key -> Lists.newArrayList()).add(subscriber);
    }

    public static void unsubscribe(String deviceId, Consumer<Double> subscriber) {
        subscribers.computeIfAbsent(deviceId, key -> Lists.newArrayList()).remove(subscriber);
    }

    public static double getOutsideTemperature() {
        return recentTemperatures.getOrDefault(Db.applyDao(new HeatingDao(), dao -> dao.getPlaceRefCd(TempRefCode.EXTERNAL_TEMPERATURE_PLACE_REF_CD).getDeviceId()), new AtomicDouble(INITIAL_VALUE)).get();
    }

    public static double getTemperature(String deviceId) {
        return recentTemperatures.getOrDefault(deviceId, new AtomicDouble(INITIAL_VALUE)).get();
    }

    public static void setTemperature(String deviceId, double value) {
        AtomicDouble tempValue = recentTemperatures.computeIfAbsent(deviceId, key -> new AtomicDouble(INITIAL_VALUE));
        double oldValue = tempValue.getAndSet(value);
        if (Double.compare(oldValue, value) != 0) {
            subscribers.computeIfAbsent(deviceId, key -> Lists.newArrayList()).forEach(subscriber -> subscriber.accept(value));
        }
    }
}

