package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.TimerService;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.solar.DayLight;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.temp.TempService;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SolarPanel {
    public static final Logger LOGGER = Logger.getLogger(SolarPanel.class.getName());

    private static String T1_MEASURE_PLACE;
    private static String T2_MEASURE_PLACE;
    private static Supplier<Double> T1_SOLAR_THRESHOLD;
    private static Supplier<Double> T2_WATER_THRESHOLD;
    private static Supplier<Double> T1_T2_DIFF_TEMP;
    private static Supplier<Double> T2_WARM_WATER_TEMP;
    private static Supplier<Double> T2_WARM_WATER_DIFF_TEMP;

    private final SolarPanelStateManager panelStateManager;

    public SolarPanel(SolarPanelStateManager panelStateManager) {
        this.panelStateManager = panelStateManager;
    }

    public void start() {
        T1_MEASURE_PLACE = Db.applyDao(new SettingsDao(), dao -> dao.getString("T1_MEASURE_PLACE"));
        T2_MEASURE_PLACE = Db.applyDao(new SettingsDao(), dao -> dao.getString("T2_MEASURE_PLACE"));
        T1_SOLAR_THRESHOLD = () -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble("T1_SOLAR_THRESHOLD"));
        T2_WATER_THRESHOLD = () -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble("T2_WATER_THRESHOLD"));
        T1_T2_DIFF_TEMP = () -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble("T1_T2_DIFF_TEMP"));
        T2_WARM_WATER_TEMP = () -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble("T2_WARM_WATER_TEMP"));
        T2_WARM_WATER_DIFF_TEMP = () -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble("T2_WARM_WATER_DIFF_TEMP"));


        DayLight.inst().subscribe((state) -> {
            if (state)
                panelStateManager.add(SolarPanelStateManager.Event.DAY_LIGHT_REACHED);
        });
        String deviceIdT1 = getDeviceId(T1_MEASURE_PLACE);
        String deviceIdT2 = getDeviceId(T2_MEASURE_PLACE);
        TempService.subscribe(deviceIdT1, value -> {
            LOGGER.fine(T1_MEASURE_PLACE + " teplota je " + value);
            double t2temp = TempService.getTemperature(deviceIdT2);
            if ((value - t2temp) >= T1_T2_DIFF_TEMP.get() && panelStateManager.has(SolarPanelStateManager.Event.WATER_OVERHEATED)) {
                panelStateManager.add(SolarPanelStateManager.Event.SOLAR_PUMP_MALFUNCTION);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.SOLAR_PUMP_MALFUNCTION);
            }
            if (value > T1_SOLAR_THRESHOLD.get()) {
                panelStateManager.add(SolarPanelStateManager.Event.PANEL_OVERHEATED);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.PANEL_OVERHEATED);
            }
        });
        TempService.subscribe(deviceIdT2, value -> {
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            double t1temp = TempService.getTemperature(deviceIdT1);
            Double warmWaterTempTreshhold = T2_WARM_WATER_TEMP.get();
            if (value > warmWaterTempTreshhold) {
                panelStateManager.add(SolarPanelStateManager.Event.WARM_WATER);
            } else {
                double overheatedDiff = warmWaterTempTreshhold - value;
                if (panelStateManager.has(SolarPanelStateManager.Event.WARM_WATER)
                        && overheatedDiff >T2_WARM_WATER_DIFF_TEMP.get())
                panelStateManager.remove(SolarPanelStateManager.Event.WARM_WATER);
            }
            if (value > T2_WATER_THRESHOLD.get()) {
                panelStateManager.add(SolarPanelStateManager.Event.WATER_OVERHEATED);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.WATER_OVERHEATED);
            }
            if ((t1temp - value) >= T1_T2_DIFF_TEMP.get() && panelStateManager.has(SolarPanelStateManager.Event.WATER_OVERHEATED)) {
                panelStateManager.add(SolarPanelStateManager.Event.SOLAR_PUMP_MALFUNCTION);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.SOLAR_PUMP_MALFUNCTION);
            }
        });
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                panelStateManager.dailyReset();
                SolarPanel.this.ticker();
                DayLight.inst().clear();
                TimerService.scheduleForNextDay("Solar denny reset", this);
            }
        };
        TimerService.scheduleForNextDay("Solar denny reset", runnable);
        StrongWind.addSubscriber((state) -> {
            if (state) {
                panelStateManager.add(SolarPanelStateManager.Event.STRONG_WIND);
                panelStateManager.add(SolarPanelStateManager.Event.WAS_WINDY);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.STRONG_WIND);
            }
        });
        ticker();
        if (DayLight.inst().enoughLight()) panelStateManager.add(SolarPanelStateManager.Event.DAY_LIGHT_REACHED, false);
        if (StrongWind.isWindy()) panelStateManager.add(SolarPanelStateManager.Event.STRONG_WIND, false);
        panelStateManager.nextTick();
    }

    private void ticker() {
        Optional<SolarPanelStateManager.Record> nextStep = panelStateManager.calculatePosition();
        if (!nextStep.isPresent()) LOGGER.log(Level.CONFIG, "Žiadny ďaľší posun na tento deň");
        nextStep.ifPresent(step -> {
            if (step.getNextMoveHour() != null) {
                TimerService.scheduleFor("Solar - ďalší posun", step.getNextMoveHour(), step.getNextMoveMinute(), () -> {
                    panelStateManager.nextTick();
                    ticker();
                });
            } else {
                LOGGER.log(Level.CONFIG, "Žiadny ďaľší posun na tento deň");
            }
        });
    }


    String getDeviceId(String deviceId) {
        return Db.applyDao(new HeatingDao(), dao -> dao.getDeviceId(deviceId));
    }
}
