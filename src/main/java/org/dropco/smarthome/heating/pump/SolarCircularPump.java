package org.dropco.smarthome.heating.pump;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.ServiceMode;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.ThreeWayValve;
import org.dropco.smarthome.temp.TempService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SolarCircularPump implements Runnable {
    protected static final String CIRCULAR_PUMP_DIFF_START_TEMP = "SOLAR_CIRCULAR_PUMP_DIFF_START_TEMP";
    protected static final String CIRCULAR_PUMP_DIFF_STOP_TEMP = "SOLAR_CIRCULAR_PUMP_DIFF_STOP_TEMP";
    public static final String CIRCULAR_PUMP_PORT = "SOLAR_CIRCULAR_PUMP_PORT";
    protected static final String CIRCULAR_PUMP_OVERHEATED_CYCLE_ON = "CIRCULAR_PUMP_OVERHEATED_CYCLE_ON";
    protected static final String CIRCULAR_PUMP_OVERHEATED_CYCLE_OFF = "CIRCULAR_PUMP_OVERHEATED_CYCLE_OFF";

    static String T1_MEASURE_PLACE;
    static String T2_MEASURE_PLACE;
    public static final Logger LOGGER = Logger.getLogger(SolarCircularPump.class.getName());

    static AtomicBoolean state = new AtomicBoolean(false);
    AtomicDouble tempT1 = new AtomicDouble(0);
    AtomicDouble tempT2 = new AtomicDouble(0);
    AtomicBoolean overheated = new AtomicBoolean(false);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());

    public SolarCircularPump(BiConsumer<String, Boolean> commandExecutor) {
        Db.acceptDao(new SettingsDao(), dao -> {
            T1_MEASURE_PLACE = dao.getString("T1_MEASURE_PLACE");
            T2_MEASURE_PLACE = dao.getString("T2_MEASURE_PLACE");
        });
        this.commandExecutor = commandExecutor;
        ServiceMode.addSubsriber(mode -> {
            if (mode) {
                raiseChange(false);
            } else {
                raiseChange(state.get());
            }

        });
    }


    @Override
    public void run() {
        String tiDeviceId = getDeviceId(T1_MEASURE_PLACE);
        TempService.subscribe(tiDeviceId, value -> {
            tempT1.set(value);
            LOGGER.fine(T1_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        String t2DeviceId = getDeviceId(T2_MEASURE_PLACE);
        TempService.subscribe(t2DeviceId, value -> {
            tempT2.set(value);
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        ThreeWayValve.addSubscriber(value -> {
            update.release();
        });
        tempT1.set(TempService.getTemperature(tiDeviceId));
        tempT2.set(TempService.getTemperature(t2DeviceId));
        while (true) {
            if (overheated.get()) {
                if (state.compareAndSet(false, true)) {
                    if (!ServiceMode.isServiceMode()) {
                        raiseChange(true);
                        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> update.release(), Db.applyDao(new SettingsDao(), dao -> dao.getLong(CIRCULAR_PUMP_OVERHEATED_CYCLE_ON)), TimeUnit.MILLISECONDS);
                    }
                } else if (state.compareAndSet(true, false)) {
                    if (!ServiceMode.isServiceMode()) {
                        raiseChange(false);
                        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(() -> update.release(), Db.applyDao(new SettingsDao(), dao -> dao.getLong(CIRCULAR_PUMP_OVERHEATED_CYCLE_OFF)), TimeUnit.MILLISECONDS);

                    }
                }
            } else {
                double difference = tempT1.get() - tempT2.get();
                LOGGER.fine("Rozdiel teplôt pre obehové čerpadlo je " + difference);
                if (difference >= getStartThreshold() && state.compareAndSet(false, true)) {
                    if (!ServiceMode.isServiceMode()) {
                        raiseChange(true);
                    }
                }
                if (difference <= getStopThreshold() && !ThreeWayValve.getState() && state.compareAndSet(true, false)) {
                    if (!ServiceMode.isServiceMode()) {
                        raiseChange(false);
                    }
                }
            }
            update.acquireUninterruptibly();
        }
    }

    void raiseChange(boolean state) {
        LOGGER.info("Obehové čerpadlo kolektorov sa " + (state ? "zapne" : "vypne"));
        commandExecutor.accept(CIRCULAR_PUMP_PORT, state);
        subscribers.forEach(consumer -> consumer.accept(state));
    }

    void setState(boolean newState) {
        state.set(newState);
    }


    String getDeviceId(String placeRefCd) {
        return Db.applyDao(new HeatingDao(), dao -> dao.getDeviceByPlaceRefCd(placeRefCd).getId());
    }

    double getStopThreshold() {
        return Db.applyDao(new SettingsDao(), dao -> dao.getDouble(CIRCULAR_PUMP_DIFF_STOP_TEMP));
    }

    double getStartThreshold() {
        return Db.applyDao(new SettingsDao(), dao -> dao.getDouble(CIRCULAR_PUMP_DIFF_START_TEMP));
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }


    public static boolean getState() {
        return state.get();
    }
}
