package org.dropco.smarthome.heating;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.temp.TempService;

import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CircularPump implements Runnable {
    protected static final String CIRCULAR_PUMP_DIFF_START_TEMP = "CIRCULAR_PUMP_DIFF_START_TEMP";
    protected static final String CIRCULAR_PUMP_DIFF_STOP_TEMP = "CIRCULAR_PUMP_DIFF_STOP_TEMP";
    protected static final String CIRCULAR_PUMP_PORT = "CIRCULAR_PUMP_PORT";

    static final String T1_TEMP_KEY = "SOLAR";
    static final String T2_TEMP_KEY = "TA3";
    public static final Logger LOGGER = Logger.getLogger(CircularPump.class.getName());

    static AtomicBoolean state = new AtomicBoolean(false);
    AtomicDouble tempT1 = new AtomicDouble(0);
    AtomicDouble tempT2 = new AtomicDouble(0);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Lists.newArrayList();
    private SettingsDao settingsDao;

    public CircularPump(SettingsDao settingsDao, BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        ServiceMode.addSubsriber(mode -> {
            if (state.get() && mode) {
                commandExecutor.accept(CIRCULAR_PUMP_PORT, false);
            }
        });
    }


    @Override
    public void run() {
        TempService.subscribe(getDeviceId(T1_TEMP_KEY), value1 -> {
            tempT1.set(value1);
            LOGGER.info(T1_TEMP_KEY + " teplata je " + value1);
            update.release();
        });
        TempService.subscribe(getDeviceId(T2_TEMP_KEY), value -> {
            tempT2.set(value);
            LOGGER.info(T2_TEMP_KEY + " teplata je " + value);
            update.release();
        });
        tempT1.set(TempService.getTemperature(getDeviceId(T1_TEMP_KEY)));
        tempT2.set(TempService.getTemperature(getDeviceId(T2_TEMP_KEY)));
        LOGGER.info(T1_TEMP_KEY + " teplata je " + tempT1.get());
        LOGGER.info(T2_TEMP_KEY + " teplata je " + tempT2.get());
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                double difference = tempT1.get() - tempT2.get();
                LOGGER.info("Rozdiel teplot pre obehové čerpadlo je" + difference);
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

    void raiseChange(boolean b) {
        LOGGER.info("Čerpadlo sa "+ (b?"zapne":"vypne"));
        commandExecutor.accept(CIRCULAR_PUMP_PORT, b);
        subscribers.forEach(consumer -> consumer.accept(b));
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
