package org.dropco.smarthome.heating.solar.move;

import com.google.common.util.concurrent.AtomicDouble;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.heating.db.HeatingDao;
import org.dropco.smarthome.heating.dto.AbsolutePosition;
import org.dropco.smarthome.heating.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.DayLight;
import org.dropco.smarthome.heating.solar.StrongWind;
import org.dropco.smarthome.temp.TempService;

import java.util.Calendar;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static org.dropco.smarthome.heating.solar.move.SolarPanelState.SolarPanelEvent.*;

public class SolarPanel {
    public static final Logger LOGGER = Logger.getLogger(SolarPanel.class.getName());
    public static final String SOLAR_PANEL_STATE = "SOLAR_PANEL_STATE";

    private Semaphore update = new Semaphore(0);
    private static String T1_MEASURE_PLACE;
    private static String T2_MEASURE_PLACE;
    private static double T1_SOLAR_THRESHOLD = 0;
    private static double T2_WATER_THRESHOLD = 0;
    private static int SOLAR_SOUTH = 430;
    private static int SOLAR_NORTH = 0;
    private static int SOLAR_WEST = 0;
    private static int SOLAR_EAST = 690;
    private static String afternoonTime;
    private AtomicDouble tempT1 = new AtomicDouble(0);
    private AtomicDouble tempT2 = new AtomicDouble(0);
    private final Supplier<DeltaPosition> strongWindProvider;
    private Mover mover;
    private SolarPanelState panelState;

    public SolarPanel(Supplier<DeltaPosition> strongWindProvider, Mover mover) {
        this.strongWindProvider = strongWindProvider;
        this.mover = mover;


    }

    public void run() {
        Db.acceptDao(new SettingsDao(), dao -> {
            T1_MEASURE_PLACE = dao.getString("T1_MEASURE_PLACE");
            T2_MEASURE_PLACE = dao.getString("T2_MEASURE_PLACE");
            panelState = SolarPanelState.valueOf(dao.getString(SOLAR_PANEL_STATE));
            afternoonTime = dao.getString("SOLAR_AFTERNOON_TIME");
            T1_SOLAR_THRESHOLD = dao.getDouble("T1_SOLAR_THRESHOLD");
            T2_WATER_THRESHOLD = dao.getDouble("T2_WATER_THRESHOLD");
            SOLAR_SOUTH = (int) dao.getDouble("SOLAR_SOUTH");
            SOLAR_NORTH = (int) dao.getDouble("SOLAR_NORTH");
            SOLAR_WEST = (int) dao.getDouble("SOLAR_WEST");
            SOLAR_EAST = (int) dao.getDouble("SOLAR_EAST");
        });
        DayLight.inst().subscribe((state) -> update.release());
        String deviceIdT1 = getDeviceId(T1_MEASURE_PLACE);
        tempT1.set(TempService.getTemperature(deviceIdT1));
        TempService.subscribe(deviceIdT1, value -> {
            tempT2.set(value);
            LOGGER.fine(T1_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        String deviceIdT2 = getDeviceId(T2_MEASURE_PLACE);
        tempT2.set(TempService.getTemperature(deviceIdT2));
        TempService.subscribe(deviceIdT2, value -> {
            tempT2.set(value);
            LOGGER.fine(T2_MEASURE_PLACE + " teplota je " + value);
            update.release();
        });
        StrongWind.addSubscriber((state) -> update.release());
        while (true) {
            SolarPanelState previousState = panelState;
            if (StrongWind.isWindy()) {
                panelState = panelState.getNextState(STRONG_WIND);
            } else {
                panelState = panelState.getNextState(NO_WIND);
            }
            if (DayLight.inst().enoughLight()) {
                panelState = panelState.getNextState(DAY_LIGHT_REACHED);
            }
            if (tempT2.get() > T2_WATER_THRESHOLD) {
                panelState = panelState.getNextState(WATER_OVERHEATED);
            } else {
                panelState = panelState.getNextState(WATER_COOLED_DOWN);
            }
            if (tempT1.get() > T1_SOLAR_THRESHOLD) {
                panelState = panelState.getNextState(PANEL_OVERHEATED);
            } else {
                panelState = panelState.getNextState(PANEL_COOLED_DOWN);
            }
            if (previousState != panelState)
                moveToLocation();
            update.acquireUninterruptibly();
        }
    }

    void moveToLocation() {
        Db.acceptDao(new SettingsDao(), dao -> dao.setString(SOLAR_PANEL_STATE, panelState.name()));
        switch (panelState) {
            case NORMAL:
                //get BNR
                break;
            case NORMAL_AFTER_WIND:
                //
                break;
            case STRONG_WIND:
                mover.moveTo(strongWindProvider.get());
                break;
            case PARK_POSITION:
                mover.moveTo(new AbsolutePosition(SOLAR_WEST, SOLAR_NORTH));
                break;
            case WATER_OVERHEATED:
            case PANEL_WATER_OVERHEATED:
            case PANEL_OVERHEATED:
                if (isAfternoon(Calendar.getInstance())) {
                    mover.moveTo(new AbsolutePosition(SOLAR_EAST, SOLAR_SOUTH));
                } else {
                    mover.moveTo(new AbsolutePosition(SOLAR_WEST, SOLAR_SOUTH));
                }
                break;
            case PANEL_OVERHEATED_STRONG_WIND:
            case WATER_OVERHEATED_STRONG_WIND:
            case PANEL_WATER_OVERHEATED_STRONG_WIND:
            case PANEL_OVERHEATED_STRONG_WIND_AFTER_WIND:
            case WATER_OVERHEATED_STRONG_WIND_AFTER_WIND:
            case PANEL_WATER_OVERHEATED_STRONG_WIND_AFTER_WIND:
                DeltaPosition strongWindDelta = strongWindProvider.get();
                if (isAfternoon(Calendar.getInstance())) {
                    strongWindDelta.setDeltaHorizontalTicks(SOLAR_EAST);
                    mover.moveTo(strongWindDelta);
                } else {
                    strongWindDelta.setDeltaHorizontalTicks(-SOLAR_WEST);
                    mover.moveTo(strongWindDelta);
                }
                break;
            case PANEL_OVERHEATED_AFTER_WIND:
            case WATER_OVERHEATED_AFTER_WIND:
            case PANEL_WATER_OVERHEATED_AFTER_WIND:
                if (isAfternoon(Calendar.getInstance())) {
                    mover.moveTo(new DeltaPosition(SOLAR_EAST, 0));
                } else {
                    mover.moveTo(new DeltaPosition(-SOLAR_WEST, 0));
                }
                break;
        }
    }

    String getDeviceId(String deviceId) {
        return Db.applyDao(new HeatingDao(), dao -> dao.getDeviceId(deviceId));
    }

    private boolean isAfternoon(Calendar currentTime) {
        Calendar calculatedTime = Calendar.getInstance();
        calculatedTime.set(Calendar.SECOND, 0);
        calculatedTime.set(Calendar.MILLISECOND, 0);
        String[] split = afternoonTime.split(":");
        calculatedTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(split[0]));
        calculatedTime.set(Calendar.MINUTE, Integer.parseInt(split[1]));
        return calculatedTime.after(currentTime);
    }
}
