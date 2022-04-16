package org.dropco.smarthome.watering;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.Set;
import java.util.function.Supplier;


public class WateringMain {

    private static final String WATERING_PUMP_PORT = "WATERING_PUMP_PORT";

    public static void main(SettingsDao settingsDao) {
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
        Db.applyDao(new SettingsDao(), dao->dao.getString(WATERING_PUMP_PORT));
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
            ServiceMode.addOutput(port, key ->  Main.pinManager.getOutput(key));
        });
    }


}
