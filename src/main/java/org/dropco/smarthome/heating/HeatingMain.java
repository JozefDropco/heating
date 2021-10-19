package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.heater.HeatingHeaterMain;
import org.dropco.smarthome.heating.solar.*;
import org.dropco.smarthome.heating.solar.move.SafetySolarPanel;
import org.dropco.smarthome.heating.solar.move.SolarPanelManager;
import org.dropco.smarthome.heating.solar.move.SolarPanelMover;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Calendar;
import java.util.function.Consumer;

public class HeatingMain {

    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    public static final String DAY_LIGHT_PIN_REF_CD = "DAY_LIGHT_PIN";
    protected static final String LIGHT_THRESHOLD = "LIGHT_THRESHOLD";
    protected static final String SOLAR_OVERHEATED = "SOLAR_OVERHEATED";

    public static void start(SettingsDao settingsDao) {
        SolarHeatingMain.start((key, value) -> {
            Main.getOutput(key).setState(value);
        });
        HeatingHeaterMain.start(settingsDao);
        addFireplace();
        configureServiceMode();
        addToStats();
        ServiceMode.addSubsriber(state -> {
            if (state) SolarPanelManager.stop();
        });
        DayLight.setInstance(Main.getInput(DAY_LIGHT_PIN_REF_CD), () -> Db.applyDao(new SettingsDao(), dao-> (int)dao.getLong(LIGHT_THRESHOLD)));
        connectDayLight(settingsDao);
        SolarPanelManager.delaySupplier = () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getDelay);
        SolarPanelMover.setCommandExecutor((key, value) -> Main.getOutput(key).setState(value));
        SolarPanelMover.setCurrentPositionSupplier(() -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getLastKnownPosition));
        SolarPanelMover.addListener(panel -> Db.acceptDao(new SolarSystemDao(), dao -> dao.updateLastKnownPosition(panel)));
        SafetySolarPanel safetySolarPanel = new SafetySolarPanel(position -> Db.acceptDao(new SolarSystemDao(), dao->dao.saveNormalPosition(position)), () -> Db.applyDao(new SolarSystemDao(), dao->dao.getStrongWindPosition()),
                () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getLastKnownPosition),
                () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getOverheatedPosition));
        StrongWind.connect(Main.getInput(STRONG_WIND_PIN_REF_CD), safetySolarPanel);
        new SolarTemperatureWatch(() ->  Db.applyDao(new SettingsDao(),dao->dao.getDouble(SOLAR_OVERHEATED))).attach(safetySolarPanel);
        SolarSystemScheduler solarSystemScheduler = new SolarSystemScheduler();
        solarSystemScheduler.moveToLastPosition(safetySolarPanel);
        solarSystemScheduler.schedule(safetySolarPanel);
        DayLight.inst().subscribe(enoughLight -> {
            if (enoughLight) safetySolarPanel.backToNormal();
        });

    }

    private static void addFireplace() {
        new FireplaceCircularPump(Main.getInput(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD)).start();
        ServiceMode.addInput(new NamedPort(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD, "Krb chod čerpadla"), () -> FireplaceCircularPump.getState());
        StatsCollector.getInstance().collect("Krb chod čerpadla", FireplaceCircularPump.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> countStats) {
                FireplaceCircularPump.addSubscriber(countStats);
            }
        });
    }

    private static void configureServiceMode() {
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.EAST_PIN_REF_CD, "Kolektory - Východ"), key -> Main.getOutput( key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.WEST_PIN_REF_CD, "Kolektory - Západ"), key -> Main.getOutput( key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.NORTH_PIN_REF_CD, "Kolektory - Sever"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.SOUTH_PIN_REF_CD, "Kolektory - Juh"), key -> Main.getOutput(key));
        ServiceMode.addInput(new NamedPort(STRONG_WIND_PIN_REF_CD, "Silný vietor"), () -> Main.getInput(STRONG_WIND_PIN_REF_CD).isHigh());
        ServiceMode.addInput(new NamedPort("STRONG_WIND_LIMIT", "Silný vietor - limit splnený"), () -> StrongWind.isWindy());
        ServiceMode.addInput(new NamedPort(DAY_LIGHT_PIN_REF_CD, "Jas"), () -> DayLight.inst().getCurrentState());
        ServiceMode.addInput(new NamedPort("DAY_LIGHT_LIMIT", "Jas - limit splnený"), () -> DayLight.inst().enoughLight());
        ServiceMode.getExclusions().put(SolarSystemRefCode.EAST_PIN_REF_CD, SolarSystemRefCode.WEST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.WEST_PIN_REF_CD, SolarSystemRefCode.EAST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.NORTH_PIN_REF_CD, SolarSystemRefCode.SOUTH_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.SOUTH_PIN_REF_CD, SolarSystemRefCode.NORTH_PIN_REF_CD);
    }

    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - Sever", Main.getOutput(SolarSystemRefCode.NORTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Juh", Main.getOutput(SolarSystemRefCode.SOUTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Východ", Main.getOutput ( SolarSystemRefCode.EAST_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Západ", Main.getOutput(SolarSystemRefCode.WEST_PIN_REF_CD));
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
