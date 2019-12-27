package org.dropco.smarthome.watering;

import com.pi4j.io.gpio.*;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.Map;


public class WateringMain {

    private static final GpioController gpio = GpioFactory.getInstance();


    public static void main(SettingsDao settingsDao, Map<String, GpioPinDigitalInput> inputMap, Map<String, GpioPinDigitalOutput> outputMap, String[] args) {
        WateringThreadManager.thresholdTempValue = new SettingsDao().getDouble("TEMP_THRESHOLD");
        WateringJob.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            GpioPinDigitalOutput output = outputMap.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), key, PinState.LOW);
                outputMap.put(pinName, output);
            }
            output.setState(value);
        });
        WateringJob.setZones(new WateringDao()::getActiveZones);
        new WateringScheduler(new WateringDao()).schedule();
    }


}
