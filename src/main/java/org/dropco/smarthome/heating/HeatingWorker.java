package org.dropco.smarthome.heating;

import com.pi4j.component.temperature.TemperatureListener;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.HeatingDao;
import org.dropco.smarthome.database.SettingsDao;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD;
import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD;

public class HeatingWorker implements Runnable {
    private static final Logger logger = Logger.getLogger(HeatingWorker.class.getName());
    private final Consumer<Boolean> solarOverHeated;
    private SettingsDao settingsDao;
    private HeatingDao heatingDao =new HeatingDao();

    public HeatingWorker(Consumer<Boolean> solarOverHeated, SettingsDao settingsDao) {
        this.solarOverHeated = solarOverHeated;
        this.settingsDao = settingsDao;
    }

    @Override
    public void run() {
        W1Master master = new W1Master();
        List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
        sensors.forEach(sensor -> sensor.addListener((TemperatureListener) temperatureChangeEvent -> {
            logger.log(Level.INFO, "Temperature of " + ((W1Device) temperatureChangeEvent.getTemperatureSensor()).getId() + ":" + temperatureChangeEvent.getNewTemperature());
        }));
        while (true) {
            sensors = master.getDevices(TemperatureSensor.class);
            for (TemperatureSensor sensor : sensors) {
                W1Device device = (W1Device) sensor;
                double temperature = sensor.getTemperature(TemperatureScale.CELSIUS);
                if (device.getId().trim().equals(heatingDao.getDeviceId(SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD))) {
                    solarOverHeated.accept(temperature >= settingsDao.getDouble(SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD));
                }
            }
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
