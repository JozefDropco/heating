package org.dropco.smarthome.heating.heater;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class HeatingConfiguration {


    private static List<Consumer<SolarHeatingSchedule>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    static ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

    private static final AtomicReference<SolarHeatingSchedule> CURRENT_RECORD = new AtomicReference<>();


    public static void start() {
        SolarHeatingSchedule currentRecord = Db.applyDao(new HeatingDao(),HeatingDao::getCurrentRecord);
        HeatingConfiguration.CURRENT_RECORD.set(currentRecord);
        Logger.getLogger(HeatingConfiguration.class.getName()).info(currentRecord.toString());
        subscribers.forEach(subs-> subs.accept(currentRecord));
        SolarHeatingSchedule nextRecord = Db.applyDao(new HeatingDao(),HeatingDao::getNextRecord);
        LocalTime nextFromTime = nextRecord.getFromTime().plusSeconds(30);
        long sleepTime = LocalTime.now().until(nextFromTime, ChronoUnit.SECONDS);
        if (sleepTime<0){
            sleepTime+=24*60*60; //convert to next day
        }
        EXECUTOR_SERVICE.schedule(HeatingConfiguration::start,sleepTime,TimeUnit.SECONDS);
    }

    /***
     * Gets the currentRecord
     * @return
     */
    public static SolarHeatingSchedule getCurrent() {
        return CURRENT_RECORD.get();
    }

    public static void addSubscriber(Consumer<SolarHeatingSchedule> subscriber) {
        subscribers.add(subscriber);
    }


}
