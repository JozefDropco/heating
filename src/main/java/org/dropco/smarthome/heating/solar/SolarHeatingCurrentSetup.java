package org.dropco.smarthome.heating.solar;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SolarHeatingCurrentSetup {

    private static List<Consumer<SolarHeatingSchedule>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    static ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

    private static final AtomicReference<SolarHeatingSchedule> CURRENT_RECORD = new AtomicReference<>();


    public static void start(HeatingDao heatingDao) {
        SolarHeatingSchedule currentRecord = heatingDao.getCurrentRecord();
        SolarHeatingCurrentSetup.CURRENT_RECORD.set(currentRecord);
        Logger.getLogger(SolarHeatingCurrentSetup.class.getName()).info(currentRecord.toString());
        subscribers.forEach(subs-> subs.accept(currentRecord));
        LocalTime endTime = SolarHeatingCurrentSetup.CURRENT_RECORD.get().getToTime();
        long sleepTime = LocalTime.now().until(endTime, ChronoUnit.SECONDS)+2;
        EXECUTOR_SERVICE.schedule(() -> start(heatingDao),sleepTime,TimeUnit.SECONDS);
    }

    /***
     * Gets the currentRecord
     * @return
     */
    public static SolarHeatingSchedule get() {
        return CURRENT_RECORD.get();
    }

    public static void addSubscriber(Consumer<SolarHeatingSchedule> subscriber) {
        subscribers.add(subscriber);
    }


}
