package org.dropco.smarthome.watering;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.w1.W1Device;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.HeatingDao;
import org.dropco.smarthome.heating.HeatingRefCode;
import org.dropco.smarthome.watering.db.WateringDao;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.dropco.smarthome.watering.WateringJob.WATERING_PUMP_STOP_DELAY;

public class WateringMain {

    private static final GpioController gpio = GpioFactory.getInstance();

    private static final Logger logger = Logger.getLogger(WateringMain.class.getName());

    public static void main(SettingsDao settingsDao,  Map<String, GpioPinDigitalInput> inputMap, Map<String, GpioPinDigitalOutput> outputMap, String[] args) {
        WateringJob.setCommandExecutor((key, value) -> {
            String pinName = settingsDao.getString(key);
            System.out.println((value ? "Opening" : "Closing") + " pin for key=" + key + " on " + pinName);
            GpioPinDigitalOutput output = outputMap.get(pinName);
            if (output == null) {
                output = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName(pinName), key, PinState.LOW);
                outputMap.put(pinName, output);
            }
            output.setState(value);
        });
        WateringJob.setZones(new WateringDao()::getActiveZones);
        AtomicBoolean noWater = new AtomicBoolean(false);
        WateringJob.setNoWater(noWater);
        ScheduledExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
        String externalTempDeviceId = new HeatingDao().getDeviceId(HeatingRefCode.EXTERNAL_TEMPERATURE_PLACE_REF_CD);

        WateringJob.setCheckBeforeRun(() -> {
            GpioPinDigitalInput input = getRainSensor(settingsDao, inputMap);
            if (input.getState() == PinState.LOW) {
                logger.log(Level.INFO, "Stop watering as its raining outside");
                WateringThreadManager.stop();
                return false;
            }
            double threshold = settingsDao.getDouble(WateringJob.TEMP_THRESHOLD);
            double externalTemp = getExternalTemp(externalTempDeviceId);
            if (externalTemp < threshold) {
                logger.log(Level.INFO, "Stop watering as the temperature is below threshold of " + threshold + " Celzius (actual:" + externalTemp + " Celzius)");
                WateringThreadManager.stop();
                return false;
            }
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if (getExternalTemp(externalTempDeviceId) < threshold) {
                        logger.log(Level.INFO, "Stop watering as the temperature is below threshold of " + threshold + " Celzius (actual:" + externalTemp + " Celzius)");
                        WateringThreadManager.stop();
                    }
                    if (WateringThreadManager.getCurrent().isAlive())
                        executorService.schedule(this, 5, TimeUnit.SECONDS);
                }
            }, 5, TimeUnit.SECONDS);
            return true;
        });
        GpioPinDigitalInput input = getRainSensor(settingsDao,inputMap);
        if (input.getState() == PinState.LOW) {
            logger.log(Level.INFO, "Stop watering as its raining outside");
            WateringThreadManager.stop();
        }
        input.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.LOW) {
                logger.log(Level.INFO, "Stop watering as its raining outside");
                WateringThreadManager.stop();
            }
        });
        if (input.getState() == PinState.LOW) {
            logger.log(Level.INFO, "Stop watering as its raining outside");
            WateringThreadManager.stop();
        }

        input = getWaterPumpFeedback(settingsDao,inputMap);
        AtomicBoolean wasActive = new AtomicBoolean(input.getState() == PinState.HIGH);
        input.addListener((GpioPinListenerDigital) event -> {
            if (event.getState() == PinState.HIGH) {
                wasActive.set(true);
            } else {
                logger.log(Level.INFO, "Stop watering as the pump is running dry");
                noWater.set(true);
                WateringThreadManager.stop();
            }
        });
        executorService.schedule(() -> {
            if (!wasActive.get()) {
                logger.log(Level.INFO, "Stop watering as the pump is running dry");
                noWater.set(true);
                WateringThreadManager.stop();
            }
        }, settingsDao.getLong(WateringJob.WATER_PUMP_WAIT_TIME), TimeUnit.MILLISECONDS);

        double threshold = settingsDao.getDouble(WateringJob.TEMP_THRESHOLD);
        double externalTemp = getExternalTemp(externalTempDeviceId);
        if (externalTemp < threshold) {
            logger.log(Level.INFO, "Stop watering as the temperature is below threshold of " + threshold + " Celzius (actual:" + externalTemp + " Celzius)");
            WateringThreadManager.stop();
        }
        WateringJob.setWaterPumpDelay(settingsDao.getLong(WATERING_PUMP_STOP_DELAY));
        new WateringScheduler(new WateringDao()).schedule();
    }


    private static GpioPinDigitalInput getWaterPumpFeedback(SettingsDao settingsDao, Map<String, GpioPinDigitalInput> inputMap) {
        String pinName;
        GpioPinDigitalInput input;
        pinName = settingsDao.getString(WateringJob.WATER_PUMP_FEEDBACK_REF_CD);
        input = inputMap.get(pinName);
        if (input == null) {
            input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), WateringJob.WATER_PUMP_FEEDBACK_REF_CD);
            inputMap.put(pinName, input);
        }
        return input;
    }

    private static GpioPinDigitalInput getRainSensor(SettingsDao settingsDao, Map<String, GpioPinDigitalInput> inputMap) {
        String pinName = settingsDao.getString(WateringJob.RAIN_SENSOR);
        GpioPinDigitalInput input = inputMap.get(pinName);
        if (input == null) {
            input = gpio.provisionDigitalInputPin(RaspiPin.getPinByName(pinName), WateringJob.RAIN_SENSOR);
            inputMap.put(pinName, input);
        }
        return input;
    }

    private static double getExternalTemp(String externalTempDeviceId) {
        W1Master master = new W1Master();
        List<TemperatureSensor> sensors = master.getDevices(TemperatureSensor.class);
        Optional<TemperatureSensor> externalTemp = FluentIterable.from(sensors).filter(sensor -> ((W1Device) sensor).getId().trim().equals(externalTempDeviceId)).first();
        if (externalTemp.isPresent()) {
            return externalTemp.get().getTemperature(TemperatureScale.CELSIUS);
        }
        return 10.0;
    }

}
