package org.dropco.smarthome.heating.solar;

import com.google.common.collect.Lists;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.dto.SolarHeatingSchedule;
import org.dropco.smarthome.heating.solar.move.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.VerticalMoveFeedback;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SolarHeatingCurrentSetup {

    private static final String NORTH_SOUTH_MOVE_INDICATOR = "NORTH_SOUTH_MOVE_INDICATOR";
    private static final String EAST_WEST_MOVE_INDICATOR = "EAST_WEST_MOVE_INDICATOR";

    private static List<Consumer<SolarHeatingSchedule>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    static ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

    private static final AtomicReference<SolarHeatingSchedule> CURRENT_RECORD = new AtomicReference<>();


    public static void start() {
        VerticalMoveFeedback.getInstance().setInput(Main.getInput(NORTH_SOUTH_MOVE_INDICATOR)).start();
        HorizontalMoveFeedback.getInstance().setInput(Main.getInput(EAST_WEST_MOVE_INDICATOR)).start();
        SolarHeatingSchedule currentRecord = Db.applyDao(new HeatingDao(),HeatingDao::getCurrentRecord);
        SolarHeatingCurrentSetup.CURRENT_RECORD.set(currentRecord);
        Logger.getLogger(SolarHeatingCurrentSetup.class.getName()).info(currentRecord.toString());
        subscribers.forEach(subs-> subs.accept(currentRecord));
        LocalTime endTime = SolarHeatingCurrentSetup.CURRENT_RECORD.get().getToTime();
        long sleepTime = LocalTime.now().until(endTime, ChronoUnit.SECONDS)+2;
        EXECUTOR_SERVICE.schedule(() -> start(),sleepTime,TimeUnit.SECONDS);
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
