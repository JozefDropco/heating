package org.dropco.smarthome.heating.solar.move;

import org.dropco.smarthome.TimerService;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.solar.DayLight;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.temp.TempService;

import java.util.Optional;
import java.util.logging.Logger;


public class SolarPanel {
    public static final Logger LOGGER = Logger.getLogger(SolarPanel.class.getName());

    private static String T1_MEASURE_PLACE;
    private static String T2_MEASURE_PLACE;
    private static double T1_SOLAR_THRESHOLD = 0;
    private static double T2_WATER_THRESHOLD = 0;

    private SolarPanelStateManager panelStateManager;

    public SolarPanel(SolarPanelStateManager panelStateManager) {
        this.panelStateManager = panelStateManager;
    }

    public void start() {
        Db.acceptDao(new SettingsDao(), dao -> {
            T1_MEASURE_PLACE = dao.getString("T1_MEASURE_PLACE");
            T2_MEASURE_PLACE = dao.getString("T2_MEASURE_PLACE");
            T1_SOLAR_THRESHOLD = dao.getDouble("T1_SOLAR_THRESHOLD");
            T2_WATER_THRESHOLD = dao.getDouble("T2_WATER_THRESHOLD");
        });
        DayLight.inst().subscribe((state) -> {
            if (state)
                panelStateManager.add(SolarPanelStateManager.Event.DAY_LIGHT_REACHED);
        });
        String deviceIdT1 = getDeviceId(T1_MEASURE_PLACE);
        TempService.subscribe(deviceIdT1, value -> {
            LOGGER.fine(T1_MEASURE_PLACE + " teplota je " + value);
            if (value > T1_SOLAR_THRESHOLD) {
                panelStateManager.add(SolarPanelStateManager.Event.PANEL_OVERHEATED);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.PANEL_OVERHEATED);
            }
        });
        String deviceIdT2 = getDeviceId(T2_MEASURE_PLACE);
        TempService.subscribe(deviceIdT2, value -> {
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            if (value > T2_WATER_THRESHOLD) {
                panelStateManager.add(SolarPanelStateManager.Event.WATER_OVERHEATED);
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.WATER_OVERHEATED);
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
            } else {
                panelStateManager.remove(SolarPanelStateManager.Event.STRONG_WIND);
            }
        });
        ticker();
        panelStateManager.nextTick();

    }

    private void ticker() {
        Optional<SolarPanelStateManager.Record> nextStep = panelStateManager.calculatePosition();
        nextStep.ifPresent(step -> {
            if (step.getNextMoveHour() != null)
                TimerService.scheduleFor("Solar - ďalší posun", step.getNextMoveHour(), step.getNextMoveMinute(), () -> {
                    panelStateManager.nextTick();
                    ticker();
                });
        });
    }


    String getDeviceId(String deviceId) {
        return Db.applyDao(new HeatingDao(), dao -> dao.getDeviceId(deviceId));
    }
}