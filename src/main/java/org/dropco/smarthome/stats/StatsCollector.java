package org.dropco.smarthome.stats;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigital;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.temp.TempService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StatsCollector {
    private static final StatsCollector instance = new StatsCollector();

    private StatsDao statsDao = new StatsDao();
    private SettingsDao settingsDao = new SettingsDao();
    private Map<String, Long> lastIdMap = Collections.synchronizedMap(new HashMap <>());
    private SimpleDateFormat format = new SimpleDateFormat("dd. MM. yyyy HH:mm:ss z");

    public  void start() throws ParseException {
        statsDao.markAllFinished(format.parse(settingsDao.getString(StatsRefCode.LAST_HEARTBEAT)));
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                settingsDao.setString(StatsRefCode.LAST_HEARTBEAT,format.format(new Date()));
            } catch (RuntimeException e){
                Logger.getLogger(TempService.class.getName()).log(Level.FINE,"Stats collector service not working",e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void collect(String name, GpioPinDigital port){
        Logger.getLogger(StatsCollector.class.getName()).log(Level.INFO,name+" zbieram statistiky.");
        GpioPinListenerDigital listener = new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent gpioPinDigitalStateChangeEvent) {
                PinState state = gpioPinDigitalStateChangeEvent.getState();
                handle(state,name);
            }
        };
        if (port.isHigh()) handle(port.getState(),name);
        port.addListener(listener);
    }


    private void handle(PinState state, String name) {
        if (state.isHigh()){
            long id =statsDao.addEntry(name, new Date());
            lastIdMap.put(name,id);
        } else {
            Long previousId = lastIdMap.remove(name);
            if (previousId!=null){
                statsDao.finishEntry(previousId,new Date());
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
