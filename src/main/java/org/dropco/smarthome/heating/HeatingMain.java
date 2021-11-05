package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.db.SolarSystemDao;
import org.dropco.smarthome.heating.heater.Boiler;
import org.dropco.smarthome.heating.heater.BoilerBlocker;
import org.dropco.smarthome.heating.heater.Flame;
import org.dropco.smarthome.heating.pump.FireplaceCircularPump;
import org.dropco.smarthome.heating.pump.HeaterCircularPump;
import org.dropco.smarthome.heating.pump.SolarCircularPump;
import org.dropco.smarthome.heating.solar.*;
import org.dropco.smarthome.heating.solar.move.*;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Calendar;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.dropco.smarthome.heating.heater.BoilerBlocker.BOILER_BLOCK_PIN;
import static org.dropco.smarthome.heating.pump.HeaterCircularPump.HEATER_CIRCULAR_REF_CD;
import static org.dropco.smarthome.heating.pump.SolarCircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.solar.ThreeWayValve.THREE_WAY_PORT;

public class HeatingMain {
    public static final VerticalMoveFeedback VERTICAL_MOVE_FEEDBACK = new VerticalMoveFeedback();
    public static final HorizontalMoveFeedback HORIZONTAL_MOVE_FEEDBACK = new HorizontalMoveFeedback();
    final static SolarPanelMover mover = new SolarPanelMover((key, value) -> Main.getOutput(key).setState(value),
            () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getLastKnownPosition),VERTICAL_MOVE_FEEDBACK,HORIZONTAL_MOVE_FEEDBACK,
            () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getDelay));
    public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    public static final String DAY_LIGHT_PIN_REF_CD = "DAY_LIGHT_PIN";
    protected static final String LIGHT_THRESHOLD = "LIGHT_THRESHOLD";
    protected static final String SOLAR_OVERHEATED = "SOLAR_OVERHEATED";
    private static final String HEATER_BLINK_STOP = "HEATER_BLINK_STOP";
    private static final String NORTH_SOUTH_MOVE_INDICATOR = "NORTH_SOUTH_MOVE_INDICATOR";
    private static final String EAST_WEST_MOVE_INDICATOR = "EAST_WEST_MOVE_INDICATOR";

    public static void start(SettingsDao settingsDao) {
        BiConsumer<String, Boolean> commandExecutor = (key, value) -> {
            Main.getOutput(key).setState(value);
        };
        VERTICAL_MOVE_FEEDBACK.start(Main.getInput(NORTH_SOUTH_MOVE_INDICATOR));
        HORIZONTAL_MOVE_FEEDBACK.start(Main.getInput(EAST_WEST_MOVE_INDICATOR));
        HeatingConfiguration.start();
        new Thread(mover).start();
        new Thread(new SolarCircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new BoilerBlocker(commandExecutor)).start();
        new Flame(Main.getInput(Flame.HEATER_FLAME_REF_CD)).start();
        long blinkStop = settingsDao.getLong(HEATER_BLINK_STOP);
        new HeaterCircularPump(Main.getInput(HEATER_CIRCULAR_REF_CD)).start(blinkStop);
        new Boiler(Main.getInput(Boiler.HEATER_BOILER_FEC_CD)).start(blinkStop);
        addFireplace();
        configureServiceMode();
        addToStats();

        ServiceMode.addSubsriber(state -> {
            if (state) mover.stop();
        });
        DayLight.setInstance(Main.getInput(DAY_LIGHT_PIN_REF_CD), () -> Db.applyDao(new SettingsDao(), dao -> (int) dao.getLong(LIGHT_THRESHOLD)));
        connectDayLight(settingsDao);
        mover.addListener(panel -> Db.acceptDao(new SolarSystemDao(), dao -> dao.updateLastKnownPosition(panel)));
//        SafetySolarPanel safetySolarPanel = new SafetySolarPanel(mover, position -> Db.acceptDao(new SolarSystemDao(), dao -> dao.saveNormalPosition(position)), () -> Db.applyDao(new SolarSystemDao(), dao -> dao.getStrongWindPosition()),
//                () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getLastKnownPosition),
//                () -> Db.applyDao(new SolarSystemDao(), SolarSystemDao::getOverheatedPosition));
//        StrongWind.connect(Main.getInput(STRONG_WIND_PIN_REF_CD));
//        new SolarTemperatureWatch(() -> Db.applyDao(new SettingsDao(), dao -> dao.getDouble(SOLAR_OVERHEATED))).attach(safetySolarPanel);
        SolarSystemScheduler solarSystemScheduler = new SolarSystemScheduler();
//        solarSystemScheduler.moveToLastPosition(safetySolarPanel);
//        solarSystemScheduler.schedule(safetySolarPanel);

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
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.EAST_PIN_REF_CD, "Kolektory - Východ"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.WEST_PIN_REF_CD, "Kolektory - Západ"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.NORTH_PIN_REF_CD, "Kolektory - Sever"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(SolarSystemRefCode.SOUTH_PIN_REF_CD, "Kolektory - Juh"), key -> Main.getOutput(key));
        ServiceMode.addInput(new NamedPort(STRONG_WIND_PIN_REF_CD, "Silný vietor"), () -> Main.getInput(STRONG_WIND_PIN_REF_CD).isHigh());
//        ServiceMode.addInput(new NamedPort("STRONG_WIND_LIMIT", "Silný vietor - limit splnený"), () -> StrongWind.isWindy());
        ServiceMode.addInput(new NamedPort(DAY_LIGHT_PIN_REF_CD, "Jas"), () -> DayLight.inst().getCurrentState());
        ServiceMode.addInput(new NamedPort("DAY_LIGHT_LIMIT", "Jas - limit splnený"), () -> DayLight.inst().enoughLight());
        ServiceMode.getExclusions().put(SolarSystemRefCode.EAST_PIN_REF_CD, SolarSystemRefCode.WEST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.WEST_PIN_REF_CD, SolarSystemRefCode.EAST_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.NORTH_PIN_REF_CD, SolarSystemRefCode.SOUTH_PIN_REF_CD);
        ServiceMode.getExclusions().put(SolarSystemRefCode.SOUTH_PIN_REF_CD, SolarSystemRefCode.NORTH_PIN_REF_CD);
        ServiceMode.addInput(new NamedPort(HEATER_CIRCULAR_REF_CD, "Kúrenie chod čerpadla"), () -> HeaterCircularPump.getState());
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_BLOCK_PIN, "Blokovanie ohrevu TA3"), key -> Main.getOutput(key));
        ServiceMode.addInput(new NamedPort(Flame.HEATER_FLAME_REF_CD, "Horák plynového kotla"), () -> Flame.getState());
        ServiceMode.addInput(new NamedPort(Boiler.HEATER_BOILER_FEC_CD, "Ohrev TA3 plynovým kotlom"), () -> Boiler.getState());

    }

    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - Sever", Main.getOutput(SolarSystemRefCode.NORTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Juh", Main.getOutput(SolarSystemRefCode.SOUTH_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Východ", Main.getOutput(SolarSystemRefCode.EAST_PIN_REF_CD));
        StatsCollector.getInstance().collect("Kolektory - Západ", Main.getOutput(SolarSystemRefCode.WEST_PIN_REF_CD));
        StatsCollector.getInstance().collect("S-J indikator", true, VERTICAL_MOVE_FEEDBACK::addRealTimeTicker);
        StatsCollector.getInstance().collect("V-Z indikator", true, HORIZONTAL_MOVE_FEEDBACK::addRealTimeTicker);

        StatsCollector.getInstance().collect("Kolektory - obehové čerpadlo", Main.getOutput(CIRCULAR_PUMP_PORT));
        StatsCollector.getInstance().collect("3-cestný ventil - Bypass", !ThreeWayValve.getState() && SolarCircularPump.getState(), addToStats -> {
            ThreeWayValve.addSubscriber(valveOpened -> {
                //valveShould be closed and pump should be running to add this to Stats otherwise we shouldnt count it to stats
                addToStats.accept(!valveOpened && SolarCircularPump.getState());
            });
            SolarCircularPump.addSubscriber(pumpRunning -> {
                //if Pump is running we should count it to stats only if valve is closed
                addToStats.accept(pumpRunning && !ThreeWayValve.getState());

            });
        });
        StatsCollector.getInstance().collect("3-cestný ventil - Ohrev", ThreeWayValve.getState() && SolarCircularPump.getState(), addToStats -> {
            ThreeWayValve.addSubscriber(valveOpened -> {
                //valveShould be opened and pump should be running to add this to Stats otherwise we shouldnt count it to stats
                addToStats.accept(valveOpened && SolarCircularPump.getState());
            });
            SolarCircularPump.addSubscriber(pumpRunning -> {
                //if Pump is running we should count it to stats only if valve is opened
                addToStats.accept(pumpRunning && ThreeWayValve.getState());

            });

        });
        StatsCollector.getInstance().collect("Blokovanie ohrevu TA3", Main.getOutput(BOILER_BLOCK_PIN));
        StatsCollector.getInstance().collect("Horák plynového kotla", Flame.getState(), countStats -> Flame.addSubscriber(countStats));
        StatsCollector.getInstance().collect("Kúrenie chod čerpadla", HeaterCircularPump.getState(), countStats -> HeaterCircularPump.addSubscriber(countStats));
        StatsCollector.getInstance().collect("Ohrev TA3 plynovým kotlom", Boiler.getState(), countStats -> Boiler.addSubscriber(countStats));
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
