package org.dropco.smarthome.heating;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.temp.TempService;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ThreeWayValve implements Runnable {
    protected static final String THREE_WAY_VALVE_DIFF_START_TEMP = "THREE_WAY_VALVE_DIFF_START_TEMP";
    protected static final String THREE_WAY_VALVE_DIFF_STOP_TEMP = "THREE_WAY_VALVE_DIFF_STOP_TEMP";
    protected static final String WEEKEND_THREE_WAY_VALVE_DIFF_STOP_TEMP = "WEEKEND_THREE_WAY_VALVE_DIFF_STOP_TEMP";
    protected static final String THREE_WAY_PORT = "THREE_WAY_PORT";
    static final String T31_TEMP_KEY = "PRED_TA3";
    static final String T2_TEMP_KEY = "TA3";
    static AtomicBoolean state = new AtomicBoolean(false);
    AtomicDouble tempT31 = new AtomicDouble(0);
    AtomicDouble tempT2 = new AtomicDouble(0);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Lists.newArrayList();
    private SettingsDao settingsDao;
    public ThreeWayValve(SettingsDao settingsDao, BiConsumer<String, Boolean> commandExecutor) {
        this.settingsDao =settingsDao;
        this.commandExecutor = commandExecutor;
        ServiceMode.addSubsriber(mode -> {
            if (state.get() && mode) {
                commandExecutor.accept(THREE_WAY_PORT, false);
            }
        });
    }


    @Override
    public void run() {
        TempService.subscribe(getDeviceId(T31_TEMP_KEY), value1 -> {
            tempT31.set(value1);
            update.release();
        });
        TempService.subscribe(getDeviceId(T2_TEMP_KEY), value -> {
            tempT2.set(value);
            update.release();
        });
        tempT31.set(TempService.getTemperature(getDeviceId(T31_TEMP_KEY)));
        tempT2.set(TempService.getTemperature(getDeviceId(T2_TEMP_KEY)));

        while (true) {
            if (!ServiceMode.isServiceMode()) {
                double difference = tempT31.get() - tempT2.get();
                if (difference >= getStartThreshold() && state.compareAndSet(false, true)) {
                    raiseChange(true);
                }
                double stopThreshold = getStopThreshold();
                if (difference <= stopThreshold && state.compareAndSet(true, false)) {
                    raiseChange(false);
                }
            }
            update.acquireUninterruptibly();
        }
    }

    void raiseChange(boolean state) {
        commandExecutor.accept(THREE_WAY_PORT, state);
        subscribers.forEach(consumer -> consumer.accept(state));
    }

    void setState(boolean newState){
        state.set(newState);
    }

    String getDeviceId(String t2TempKey) {
        return new HeatingDao().getDeviceId(t2TempKey);
    }

    double getStartThreshold() {
        return settingsDao.getDouble(THREE_WAY_VALVE_DIFF_START_TEMP);
    }

    double getStopThreshold() {
        double value = getStopTemperatureStop();
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        if (isWeekend(day)) {
            getStopWeekendTemperatureStop();
        }
        return value;
    }

    boolean isWeekend(int day) {
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY;
    }

    double getStopTemperatureStop() {
        return settingsDao.getDouble(THREE_WAY_VALVE_DIFF_STOP_TEMP);
    }

    double getStopWeekendTemperatureStop() {
        return settingsDao.getDouble(WEEKEND_THREE_WAY_VALVE_DIFF_STOP_TEMP);
    }


    public static boolean getState() {
        return state.get();
    }

    public static void addSubscriber(Consumer<Boolean> subscriber){
        subscribers.add(subscriber);
    }

    public static void removeSubscription(Consumer<Boolean> subscriber){
        subscribers.remove(subscriber);
    }
}
