package org.dropco.smarthome.heating;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.SettingsDao;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD;
import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD;

public class HeatingWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(HeatingWorker.class.getName());
    private final Consumer<Boolean> solarOverHeated;
    private SettingsDao settingsDao;
    private HeatingDao heatingDao = new HeatingDao();
    private LogDao logDao = new LogDao();
    private Map<String, Double> recentTemperatures = new HashMap<>();

    public HeatingWorker(Consumer<Boolean> solarOverHeated, SettingsDao settingsDao) {
        this.solarOverHeated = solarOverHeated;
        this.settingsDao = settingsDao;
    }

    @Override
    public void run() {
        W1Master master = new W1Master();
        while (true) {
            List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
            for (TemperatureSensor sensor : sensors) {
                W1Device device = (W1Device) sensor;
                double temperature = sensor.getTemperature(TemperatureScale.CELSIUS);
                String deviceId = device.getId().trim();
                if (deviceId.equals(heatingDao.getDeviceId(SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD))) {
                    solarOverHeated.accept(temperature >= settingsDao.getDouble(SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD));
                }
                if (!recentTemperatures.getOrDefault(deviceId, -1000.0d).equals(temperature)) {
                    recentTemperatures.put(deviceId, temperature);
                    logDao.logTemperature(deviceId, heatingDao.getPlaceRefCd(deviceId), new Date(), temperature);
                }
            }
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
