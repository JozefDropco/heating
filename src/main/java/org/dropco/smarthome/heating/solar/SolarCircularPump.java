package org.dropco.smarthome.heating.solar;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.temp.TempService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class SolarCircularPump implements Runnable {
    protected static final String CIRCULAR_PUMP_DIFF_START_TEMP = "SOLAR_CIRCULAR_PUMP_DIFF_START_TEMP";
    protected static final String CIRCULAR_PUMP_DIFF_STOP_TEMP = "SOLAR_CIRCULAR_PUMP_DIFF_STOP_TEMP";
    protected static final String CIRCULAR_PUMP_PORT = "SOLAR_CIRCULAR_PUMP_PORT";

    static String T1_MEASURE_PLACE;
    static String T2_MEASURE_PLACE;
    public static final Logger LOGGER = Logger.getLogger(SolarCircularPump.class.getName());

    static AtomicBoolean state = new AtomicBoolean(false);
    AtomicDouble tempT1 = new AtomicDouble(0);
    AtomicDouble tempT2 = new AtomicDouble(0);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    private SettingsDao settingsDao;

    public SolarCircularPump(SettingsDao settingsDao, BiConsumer<String, Boolean> commandExecutor) {
        T1_MEASURE_PLACE = settingsDao.getString("SOLAR_CIRCULAR_PUMP_T1_MEASURE_PLACE");
        T2_MEASURE_PLACE = settingsDao.getString("SOLAR_CIRCULAR_PUMP_T2_MEASURE_PLACE");
        this.settingsDao = settingsDao;
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
        String t1MeasurePlace = getDeviceId(T1_MEASURE_PLACE);
        TempService.subscribe(t1MeasurePlace, value -> {
            tempT1.set(value);
            LOGGER.fine(T1_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        TempService.subscribe(getDeviceId(T2_MEASURE_PLACE), value -> {
            tempT2.set(value);
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        tempT1.set(TempService.getTemperature(t1MeasurePlace));
        tempT2.set(TempService.getTemperature(getDeviceId(T2_MEASURE_PLACE)));
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                double difference = tempT1.get() - tempT2.get();
                LOGGER.fine("Rozdiel teplôt pre obehové čerpadlo je " + difference);
                if (difference >= getStartThreshold() && state.compareAndSet(false, true)) {
                    raiseChange(true);
                }
                if (difference <= getStopThreshold() && state.compareAndSet(true, false)) {
                    raiseChange(false);
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


    String getDeviceId(String key) {
        return new HeatingDao().getDeviceId(key);
    }

    double getStopThreshold() {
        return settingsDao.getDouble(CIRCULAR_PUMP_DIFF_STOP_TEMP);
    }

    double getStartThreshold() {
        return settingsDao.getDouble(CIRCULAR_PUMP_DIFF_START_TEMP);
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }


    public static boolean getState() {
        return state.get();
    }
}
