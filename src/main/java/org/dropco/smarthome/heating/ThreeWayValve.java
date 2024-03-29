package org.dropco.smarthome.heating;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AtomicDouble;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.heater.HeatingConfiguration;
import org.dropco.smarthome.temp.TempService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class ThreeWayValve implements Runnable {
    public static final String THREE_WAY_PORT = "THREE_WAY_PORT";
    static String T31_MEASURE_PLACE;
    static String T2_MEASURE_PLACE;

    private static final AtomicBoolean state = new AtomicBoolean(false);
    private static AtomicDouble tempT31 = new AtomicDouble(0);
    private static AtomicDouble tempT2 = new AtomicDouble(0);
    private static final Semaphore update = new Semaphore(0);
    private BiConsumer<String, Boolean> commandExecutor;
    private static List<Consumer<Boolean>> subscribers = Collections.synchronizedList(Lists.newArrayList());
    public static final Logger LOGGER = Logger.getLogger(ThreeWayValve.class.getName());

    public ThreeWayValve(BiConsumer<String, Boolean> commandExecutor) {
        Db.acceptDao(new SettingsDao(), dao -> {
            T31_MEASURE_PLACE = dao.getString("T31_MEASURE_PLACE");
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
        TempService.subscribe(getDeviceId(T31_MEASURE_PLACE), value -> {
            tempT31.set(value);
            LOGGER.fine(T31_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        TempService.subscribe(getDeviceId(T2_MEASURE_PLACE), value -> {
            tempT2.set(value);
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        tempT31.set(TempService.getTemperature(getDeviceId(T31_MEASURE_PLACE)));
        tempT2.set(TempService.getTemperature(getDeviceId(T2_MEASURE_PLACE)));

        while (true) {

            double difference = tempT31.get() - tempT2.get();
            LOGGER.fine("Rozdiel teplôt pre 3-cestný ventil je " + difference);
            if (difference >= HeatingConfiguration.getCurrent().getThreeWayValveStartDiff() && state.compareAndSet(false, true)) {
                //prepnut na ohrev
                if (!ServiceMode.isServiceMode()) raiseChange(true);
            }
            if (difference <= HeatingConfiguration.getCurrent().getThreeWayValveStopDiff() && state.compareAndSet(true, false)) {
                //prepnut na bypass
                if (!ServiceMode.isServiceMode()) raiseChange(false);
            }
            update.acquireUninterruptibly();
        }
    }

    private void raiseChange(boolean state) {
        LOGGER.info("3-cestný ventil sa prepne na " + (state ? "ohrev vody" : "bypass"));
        commandExecutor.accept(THREE_WAY_PORT, state);
        subscribers.forEach(consumer -> consumer.accept(state));
    }

    void setState(boolean newState) {
        state.set(newState);
    }

    String getDeviceId(String measurePlace) {
        return Db.applyDao(new HeatingDao(), dao-> dao.getPlaceRefCd(measurePlace).getDeviceId());
    }


    public static boolean getState() {
        return state.get();
    }

    public static void addSubscriber(Consumer<Boolean> subscriber) {
        subscribers.add(subscriber);
    }

    public static void removeSubscription(Consumer<Boolean> subscriber) {
        subscribers.remove(subscriber);
    }
}
