package org.dropco.smarthome.watering;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.Set;
import java.util.function.Supplier;


public class WateringMain {

    public static void main() {
        WateringThreadManager.thresholdTempValue = new SettingsDao().getDouble("TEMP_THRESHOLD");
        WateringJob.setCommandExecutor((key, value) -> {
            Main.getOutput(key).setState(value);
        });
        Supplier<Set<NamedPort>> getActiveZones = new WateringDao()::getActiveZones;
        Set<NamedPort> activeZones = getActiveZones.get();
        activeZones.forEach(ServiceMode::addOutput);
        WateringJob.setZones(getActiveZones);
        new WateringScheduler(new WateringDao()).schedule();
    }


}
