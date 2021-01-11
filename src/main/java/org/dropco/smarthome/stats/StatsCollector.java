package org.dropco.smarthome.stats;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsCollector {
    private static final StatsCollector instance = new StatsCollector();

    private StatsDao statsDao = new StatsDao();
    private SettingsDao settingsDao = new SettingsDao();
    private Map<String, Long> lastIdMap = Collections.synchronizedMap(new HashMap<>());
    private SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss z");

    public void start() throws ParseException {
        statsDao.markAllFinished(format.parse(settingsDao.getString(StatsRefCode.LAST_HEARTBEAT)));
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                settingsDao.setString(StatsRefCode.LAST_HEARTBEAT, format.format(new Date()));
            } catch (RuntimeException e) {
                Logger.getLogger(TempService.class.getName()).log(Level.FINE, "Stats collector service not working", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void collect(String name, GpioPinDigital port) {
        Logger.getLogger(StatsCollector.class.getName()).log(Level.INFO, "Zbieram štatistiky pre "+name );
        GpioPinListenerDigital listener = new DelayedGpioPinListener(PinState.HIGH, 1000, port) {

            @Override
            public void handleStateChange(boolean state) {
                if (state)
                    handle(PinState.HIGH, name);
                else
                    handle(PinState.LOW, name);
            }
        };
        if (port.isHigh()) handle(port.getState(), name);
        port.addListener(listener);
    }


    private void handle(PinState state, String name) {
        if (state.isHigh()) {
            if (name.length()>100){
                name=name.substring(0,99);
            }
            long id = statsDao.addEntry(name, new Date());
            Long prevId = lastIdMap.put(name, id);
            if (prevId!=null){
                statsDao.finishEntry(prevId, new Date());
            }
        } else {
            Long previousId = lastIdMap.remove(name);
            if (previousId != null) {
                statsDao.finishEntry(previousId, new Date());
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
