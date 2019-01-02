package org.dropco.smarthome.heating;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.HeatingDao;
import org.dropco.smarthome.database.SettingsDao;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD;
import static org.dropco.smarthome.heating.HeatingRefCode.SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD;

public class HeatingWorker implements Runnable {
    private final AtomicBoolean shutdownRequested;
    private final Consumer<Boolean> solarOverHeated;
    private SettingsDao settingsDao;
    private HeatingDao heatingDao;

    public HeatingWorker(AtomicBoolean shutdownRequested, Consumer<Boolean> solarOverHeated, SettingsDao settingsDao, HeatingDao heatingDao) {
        this.shutdownRequested = shutdownRequested;
        this.solarOverHeated = solarOverHeated;
        this.settingsDao = settingsDao;
        this.heatingDao = heatingDao;
    }

    @Override
    public void run() {
        W1Master master = new W1Master();
        while (!shutdownRequested.get()) {
            List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
            for (TemperatureSensor sensor : sensors) {
                W1Device device = (W1Device) sensor;
                double temperature = sensor.getTemperature(TemperatureScale.CELSIUS);
                if (heatingDao.getDeviceId(SOLAR_PANEL_TEMPERATURE_PLACE_REF_CD).equals(device.getId().trim())) {
                    solarOverHeated.accept(temperature >= settingsDao.getDouble(SOLAR_PANEL_TEMPERATURE_THRESHOLD_REF_CD));
                }
            }
            try {
                Thread.sleep(60*1000);
            } catch (InterruptedException e) {

            }
        }
    }
}
