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

public class CircularPump implements Runnable {
    protected static final String CIRCULAR_PUMP_DIFF_START_TEMP = "CIRCULAR_PUMP_DIFF_START_TEMP";
    protected static final String CIRCULAR_PUMP_DIFF_STOP_TEMP = "CIRCULAR_PUMP_DIFF_STOP_TEMP";
    protected static final String CIRCULAR_PUMP_PORT = "CIRCULAR_PUMP_PORT";

    static final String T1_TEMP_KEY = "T1_TEMP_KEY";
    static final String T2_TEMP_KEY = "T2_TEMP_KEY";

    static AtomicBoolean state = new AtomicBoolean(false);
    AtomicDouble tempT1 = new AtomicDouble(0);
    AtomicDouble tempT2 = new AtomicDouble(0);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Lists.newArrayList();

    public CircularPump(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        ServiceMode.addSubsriber(mode -> {
            if (state.get() && mode) {
                commandExecutor.accept(getCircularPumpPort(), false);
            }
        });
    }


    @Override
    public void run() {
        TempService.subscribe(getDeviceId(T1_TEMP_KEY), value1 -> {
            tempT1.set(value1);
            update.release();
        });
        TempService.subscribe(getDeviceId(T2_TEMP_KEY), value -> {
            tempT2.set(value);
            update.release();
        });
        tempT1.set(TempService.getTemperature(getDeviceId(T1_TEMP_KEY)));
        tempT2.set(TempService.getTemperature(getDeviceId(T2_TEMP_KEY)));
        while (true) {
            if (!ServiceMode.isServiceMode()) {
                double difference = tempT1.get() - tempT2.get();
                if (difference >= getStartThreshold() && state.compareAndSet(false, true)) {
                    commandExecutor.accept(getCircularPumpPort(), true);
                }
                if (difference <= getStopThreshold() && state.compareAndSet(true, false)) {
                    commandExecutor.accept(getCircularPumpPort(), false);
                }
            }
            update.acquireUninterruptibly();
        }
    }

    String getDeviceId(String key) {
        return new HeatingDao().getDeviceId(key);
    }

    double getStopThreshold() {
        return new SettingsDao().getDouble(CIRCULAR_PUMP_DIFF_STOP_TEMP);
    }

    String getCircularPumpPort() {
        return new SettingsDao().getString(CIRCULAR_PUMP_PORT);
    }

    double getStartThreshold() {
        return new SettingsDao().getDouble(CIRCULAR_PUMP_DIFF_START_TEMP);
    }

    public static void addSubscriber(Consumer<Boolean> subscriber){
        subscribers.add(subscriber);
    }


    public static boolean getState() {
        return state.get();
    }
}
