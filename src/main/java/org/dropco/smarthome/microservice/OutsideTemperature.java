package org.dropco.smarthome.microservice;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class OutsideTemperature {
    public static final AtomicDouble temperature = new AtomicDouble(10.0);
    private static Logger logger = Logger.getLogger(OutsideTemperature.class.getName());
    private static final List<Consumer<Double>> subscribers = Lists.newArrayList();

    public static void start(final String externalTempDeviceId) throws IOException {
        Logger.getLogger(W1Master.class.getName()).setLevel(Level.WARNING);
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                double newValue = getExternalTemp(externalTempDeviceId);
                double previous = temperature.getAndSet(newValue);
                if (previous!=newValue){
                    subscribers.forEach(subscriber->{
                        try {
                            subscriber.accept(newValue);
                        } catch (RuntimeException ex){
                            logger.log(Level.SEVERE,"Subscriber failed.",ex);
                        }
                    });
                }
            }
        },0,1, TimeUnit.MINUTES);

    }
    private static double getExternalTemp(String externalTempDeviceId) {
        W1Master master = new W1Master();
        List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
        Optional<TemperatureSensor> externalTemp = FluentIterable.from(sensors).filter(sensor -> ((W1Device) sensor).getId().trim().equals(externalTempDeviceId)).first();
        if (externalTemp.isPresent()) {
            return externalTemp.get().getTemperature(TemperatureScale.CELSIUS);
        }
        return -999.0;
    }

    public static double getTemperature() {
        return temperature.get();
    }

    public static void subscribe(Consumer<Double> subscriber) {
        subscribers.add(subscriber);
    }

    public static void unsubscribe(Supplier<Double> subscriber) {
        subscribers.remove(subscriber);
    }


}
