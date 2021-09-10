package org.dropco.smarthome.stats;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.gpioextension.DelayedGpioPinListener;
import org.dropco.smarthome.temp.TempService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsCollector {
    private static final StatsCollector instance = new StatsCollector();

    private Map<String, Long> lastIdMap = Collections.synchronizedMap(new HashMap<>());
    private SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss z");

    public void start(SettingsDao settingsDao) {
        Db.acceptDao(new StatsDao(), statsDao -> {
            try {
                statsDao.markAllFinished(format.parse(settingsDao.getString(StatsRefCode.LAST_HEARTBEAT)));
            } catch (ParseException e) {
                Logger.getLogger(TempService.class.getName()).log(Level.FINE, "Last Heartbeat not parsable. setting finished to current date", e);
                statsDao.markAllFinished(new Date());
            }
        });
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                Db.acceptDao(new SettingsDao(), dao -> dao.setString(StatsRefCode.LAST_HEARTBEAT, format.format(new Date())));
            } catch (RuntimeException e) {
                Logger.getLogger(TempService.class.getName()).log(Level.FINE, "Stats collector service not working", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void collect(String name, GpioPinDigital port) {
        collect(name, port, PinState.HIGH);
    }

    public void collectRealTime(String name, GpioPinDigital port, PinState pinState) {
        Logger.getLogger(StatsCollector.class.getName()).log(Level.INFO, "Zbieram štatistiky pre " + name);

        collect(pinState.equals(port.getState()), name);
        port.addListener((GpioPinListenerDigital) event -> collect(event.getState() == pinState, name));
    }

    public void collect(String name, GpioPinDigital port, PinState pinState) {
        Logger.getLogger(StatsCollector.class.getName()).log(Level.INFO, "Zbieram štatistiky pre " + name);
        GpioPinListenerDigital listener = new DelayedGpioPinListener(pinState, 1000, port) {
            @Override
            public void handleStateChange(boolean state) {
                collect(state, name);
            }
        };
        collect(pinState.equals(port.getState()), name);
        port.addListener(listener);
    }

    public void collect(String name, boolean startNow, Consumer<Consumer<Boolean>> subscriber) {
        Logger.getLogger(StatsCollector.class.getName()).log(Level.INFO, "Zbieram štatistiky pre " + name);
        subscriber.accept(state -> {
            collect(state, name);
        });
        collect(startNow, name);
    }

    protected void collect(boolean state, String name) {
        if (!ServiceMode.isServiceMode()) {
            if (state)
                handle(state, name);
            else
                handle(state, name);
        }
    }


    private void handle(boolean state, String name) {
        if (state) {
            if (name.length() > 100) {
                name = name.substring(0, 99);
            }
            String finalName = name;
            Db.acceptDao(new StatsDao(), statsDao -> {
                long id = statsDao.addEntry(finalName, new Date());
                Long prevId = lastIdMap.put(finalName, id);
                if (prevId != null) {
                    statsDao.finishEntry(prevId, new Date());
                }
            });
        } else {
            Long previousId = lastIdMap.remove(name);
            if (previousId != null) {
                Db.acceptDao(new StatsDao(), statsDao -> statsDao.finishEntry(previousId, new Date()));
            }
        }
    }

    /***
     * Gets the instance
     * @return
     */
    public static StatsCollector getInstance() {
        return instance;
    }
}
