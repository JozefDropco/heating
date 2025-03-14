package org.dropco.smarthome.heating.solar;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.db.SolarSystemDao;
import org.dropco.smarthome.heating.heater.HeatingConfiguration;
import org.dropco.smarthome.heating.solar.move.*;
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMove;
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMove;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMoveFeedback;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Calendar;
import java.util.logging.Logger;

import static org.dropco.smarthome.heating.solar.SolarSystemRefCode.*;

public class SolarMain {
    private static final VerticalMoveFeedback VERTICAL_MOVE_FEEDBACK = new VerticalMoveFeedback();
    private static final HorizontalMoveFeedback HORIZONTAL_MOVE_FEEDBACK = new HorizontalMoveFeedback();
    private static final VerticalMove VERTICAL_MOVE = new VerticalMove(VERTICAL_MOVE_FEEDBACK, Logger.getLogger("Solar"), Main.pinManager,tick->Db.acceptDao(new SolarSystemDao(), dao -> dao.updateLastKnownVerticalPosition(tick)),
            v -> Db.acceptDao(new SolarSystemDao(), SolarSystemDao::flushPosition));
    private static final HorizontalMove HORIZONTAL_MOVE = new HorizontalMove(HORIZONTAL_MOVE_FEEDBACK, Logger.getLogger("Solar"), Main.pinManager,tick->Db.acceptDao(new SolarSystemDao(), dao -> dao.updateLastKnownHorizontalPosition(tick)),v -> Db.acceptDao(new SolarSystemDao(), SolarSystemDao::flushPosition));

    public static final SolarPanelMover mover = new SolarPanelMover(() -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getLastKnownPosition),HORIZONTAL_MOVE,VERTICAL_MOVE);
    private static final String NORTH_SOUTH_MOVE_INDICATOR = "NORTH_SOUTH_MOVE_INDICATOR";
    private static final String EAST_WEST_MOVE_INDICATOR = "EAST_WEST_MOVE_INDICATOR";
    public static final String CURRENT_EVENTS = "SOLAR_CURRENT_EVENTS";
    public static final String TODAYS_SCHEDULE = "SOLAR_TODAYS_SCHEDULE";
    public static final String AFTERNOON_TIME = "SOLAR_AFTERNOON_TIME";
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    public static final String DAY_LIGHT_PIN_REF_CD = "DAY_LIGHT_PIN";
    protected static final String LIGHT_THRESHOLD = "LIGHT_THRESHOLD";
    public static SolarPanelStateManager panelStateManager;

    public static void start(SettingsDao settingsDao) {
        VERTICAL_MOVE_FEEDBACK.start(Main.pinManager.getInput(NORTH_SOUTH_MOVE_INDICATOR));
        HORIZONTAL_MOVE_FEEDBACK.start(Main.pinManager.getInput(EAST_WEST_MOVE_INDICATOR));
        HORIZONTAL_MOVE.start();
        VERTICAL_MOVE.start();
        DayLight.setInstance(Main.pinManager.getInput(DAY_LIGHT_PIN_REF_CD), () -> Db.applyDao(new SettingsDao(), dao -> dao.getLong(LIGHT_THRESHOLD).intValue()));
        StrongWind.connect(Main.pinManager.getInput(STRONG_WIND_PIN_REF_CD));
        connectDayLight(settingsDao);
        configureServiceMode();
        addToStats();
        ServiceMode.addSubsriber(state -> {
            if (state) mover.stop();
        });
        panelStateManager = new SolarPanelStateManager(settingsDao.getString(AFTERNOON_TIME), mover,
                () -> Db.applyDao(new SettingsDao(), sdao -> sdao.getString(TODAYS_SCHEDULE)),
                () -> Db.applyDao(new SolarSystemDao(), sdao -> sdao.getTodaysSchedule(Calendar.getInstance().get(Calendar.MONTH)+1)),
                (json) -> Db.acceptDao(new SettingsDao(), sdao -> sdao.setString(TODAYS_SCHEDULE, json)),
                () -> Db.applyDao(new SettingsDao(), sdao -> sdao.getString(CURRENT_EVENTS)),
                (json) -> Db.acceptDao(new SettingsDao(), sdao -> sdao.setString(CURRENT_EVENTS, json))
        );
        new SolarPanel(panelStateManager).start();
    }


    private static void configureServiceMode() {
        ServiceMode.addOutput(new NamedPort(EAST_PIN_REF_CD, "Kolektory - Východ"), () -> Main.pinManager.getOutput(EAST_PIN_REF_CD).isHigh(),(state) -> panelStateManager.move(Movement.EAST,state));
        ServiceMode.addOutput(new NamedPort(WEST_PIN_REF_CD, "Kolektory - Západ"), () -> Main.pinManager.getOutput(WEST_PIN_REF_CD).isHigh(),(state) -> panelStateManager.move(Movement.WEST,state));
        ServiceMode.addOutput(new NamedPort(NORTH_PIN_REF_CD, "Kolektory - Sever"), () -> Main.pinManager.getOutput(NORTH_PIN_REF_CD).isHigh(),(state) -> panelStateManager.move(Movement.NORTH,state));
        ServiceMode.addOutput(new NamedPort(SOUTH_PIN_REF_CD, "Kolektory - Juh"), () -> Main.pinManager.getOutput(SOUTH_PIN_REF_CD).isHigh(),(state) -> panelStateManager.move(Movement.SOUTH,state));

        ServiceMode.addInput(new NamedPort(STRONG_WIND_PIN_REF_CD, "Silný vietor"), () -> Main.pinManager.getInput(STRONG_WIND_PIN_REF_CD).isLow());
        ServiceMode.addInput(new NamedPort(DAY_LIGHT_PIN_REF_CD, "Jas"), () -> DayLight.inst().getCurrentState());
        ServiceMode.addInput(new NamedPort("DAY_LIGHT_LIMIT", "Jas - limit splnený"), () -> DayLight.inst().enoughLight());
        ServiceMode.addInput(new NamedPort(NORTH_SOUTH_MOVE_INDICATOR, "Pohyb S-J"), () -> VERTICAL_MOVE_FEEDBACK.isMoving());
        ServiceMode.addInput(new NamedPort(EAST_WEST_MOVE_INDICATOR, "Pohyb V-Z"), () -> HORIZONTAL_MOVE_FEEDBACK.isMoving());

    }

    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - Sever", Main.pinManager.getOutput(NORTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Juh", Main.pinManager.getOutput(SOUTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Východ", Main.pinManager.getOutput(EAST_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Západ", Main.pinManager.getOutput(WEST_PIN_REF_CD));
        StatsCollector.getInstance().collect("S-J indikator", VERTICAL_MOVE_FEEDBACK.isMoving(), VERTICAL_MOVE_FEEDBACK::addMovingSubscriber);
        StatsCollector.getInstance().collect("V-Z indikator", HORIZONTAL_MOVE_FEEDBACK.isMoving(), HORIZONTAL_MOVE_FEEDBACK::addMovingSubscriber);
    }

    private static void connectDayLight(SettingsDao settingsDao) {
        boolean dayLight = settingsDao.getLong(SolarSystemRefCode.DAYLIGHT) == 1;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        boolean modifiedAfter = settingsDao.isLongModifiedAfter(SolarSystemRefCode.DAYLIGHT, calendar.getTime());
        if (!modifiedAfter) dayLight = false;
        DayLight.inst().connect(dayLight);
    }

}
