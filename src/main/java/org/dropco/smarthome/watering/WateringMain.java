package org.dropco.smarthome.watering;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.stats.StatsCollector;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.Set;
import java.util.function.Supplier;


public class WateringMain {

    public static void main(SettingsDao settingsDao) {
        WateringThreadManager.thresholdTempValue = settingsDao.getDouble("TEMP_THRESHOLD");
        WateringJob.setCommandExecutor((key, value) -> {
            Main.getOutput(key).setState(value);
        });
        Supplier<Set<NamedPort>> getActiveZones = new WateringDao()::getActiveZones;
        Set<NamedPort> activeZones = getActiveZones.get();
        activeZones.forEach(port-> {
            ServiceMode.addOutput(port, key -> Main.getOutput(key));
            StatsCollector.getInstance().collect(port.getName(),Main.getOutput(port.getRefCd()));
        });
        WateringJob.setZones(getActiveZones);
        ServiceMode.addSubsriber(state-> {if (state) WateringThreadManager.stop();});
        new WateringScheduler(new WateringDao()).schedule();
    }


}
