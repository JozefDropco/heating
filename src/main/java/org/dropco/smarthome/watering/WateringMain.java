package org.dropco.smarthome.watering;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.function.Supplier;


public class WateringMain {


    public static void main(SettingsDao settingsDao) {
        Logger log = Logger.getLogger("com.pi4j.component.temperature.impl");
        log.setLevel(Level.OFF);
        log = Logger.getLogger("com.pi4j.io.w1");
        log.setLevel(Level.OFF);
        WaterPumpFeedback.start( Main.pinManager.getInput(WaterPumpFeedback.getMicroServicePinKey()));
        RainSensor.start( Main.pinManager.getInput(RainSensor.getMicroServicePinKey()));
        WateringThreadManager.thresholdTempValue = settingsDao.getDouble("TEMP_THRESHOLD");
        WateringJob.setCommandExecutor((key, value) -> {
             Main.pinManager.getOutput(key).setState(value);
        });
        Supplier<Set<NamedPort>> getActiveZones = ()-> Db.applyDao(new WateringDao(), WateringDao::getActiveZones);
        Set<NamedPort> activeZones = getActiveZones.get();
        configureServiceMode(activeZones);
        new WateringScheduler().schedule();
        addToStatsCollector(activeZones);
        WateringJob.setZones(getActiveZones);
    }

    private static void addToStatsCollector(Set<NamedPort> activeZones) {
        activeZones.forEach(port-> {
            StatsCollector.getInstance().collect(port.getName(), Main.pinManager.getOutput(port.getRefCd()));
        });
    }

    private static void configureServiceMode(Set<NamedPort> activeZones) {
        ServiceMode.addInput(new NamedPort(WaterPumpFeedback.getMicroServicePinKey(), "Stav čerpadla"), () ->  Main.pinManager.getInput(WaterPumpFeedback.getMicroServicePinKey()).getState() == WaterPumpFeedback.LOGICAL_HIGH_STATE);
        ServiceMode.addInput(new NamedPort(RainSensor.getMicroServicePinKey(), "Dažďový senzor"), () ->  Main.pinManager.getInput(RainSensor.getMicroServicePinKey()).getState() == RainSensor.RAIN_STATE);
        ServiceMode.addSubsriber(state-> {if (state) WateringThreadManager.stop();});
        activeZones.forEach(port-> {
            ServiceMode.addOutput(port, Main.pinManager::getOutput);
        });
    }


}
