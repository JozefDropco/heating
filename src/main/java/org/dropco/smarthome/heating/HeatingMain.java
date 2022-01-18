package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
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
import org.dropco.smarthome.heating.solar.move.horizontal.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.vertical.VerticalMoveFeedback;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.Calendar;
import java.util.function.BiConsumer;

import static org.dropco.smarthome.heating.heater.BoilerBlocker.BOILER_BLOCK_PIN;
import static org.dropco.smarthome.heating.pump.HeaterCircularPump.HEATER_CIRCULAR_REF_CD;
import static org.dropco.smarthome.heating.pump.SolarCircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.solar.ThreeWayValve.THREE_WAY_PORT;

public class HeatingMain {
    public static final VerticalMoveFeedback VERTICAL_MOVE_FEEDBACK = new VerticalMoveFeedback();
    public static final HorizontalMoveFeedback HORIZONTAL_MOVE_FEEDBACK = new HorizontalMoveFeedback();
     public static final String STRONG_WIND_PIN_REF_CD = "STRONG_WIND_PIN";
    public static final String DAY_LIGHT_PIN_REF_CD = "DAY_LIGHT_PIN";
    protected static final String LIGHT_THRESHOLD = "LIGHT_THRESHOLD";

    public static void start(SettingsDao settingsDao) {
        BiConsumer<String, Boolean> commandExecutor = (key, value) -> {
            Main.pinManager.setState(key, value);
        };
        HeatingConfiguration.start();
        new Thread(new SolarCircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new BoilerBlocker(commandExecutor)).start();
        new Flame(Main.pinManager.getInput(Flame.HEATER_FLAME_REF_CD)).start();
        new HeaterCircularPump(Main.pinManager.getInput(HEATER_CIRCULAR_REF_CD)).start();
        new Boiler(Main.pinManager.getInput(Boiler.HEATER_BOILER_FEC_CD)).start();
        addFireplace();
        configureServiceMode();
        addToStats();
        DayLight.setInstance(Main.pinManager.getInput(DAY_LIGHT_PIN_REF_CD), () -> Db.applyDao(new SettingsDao(), dao -> (int) dao.getLong(LIGHT_THRESHOLD)));
        StrongWind.connect(Main.pinManager.getInput(STRONG_WIND_PIN_REF_CD));
        connectDayLight(settingsDao);
        SolarMain.start(settingsDao);
    }

    private static void addFireplace() {
        new FireplaceCircularPump(Main.pinManager.getInput(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD)).start();
        ServiceMode.addInput(new NamedPort(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD, "Krb chod čerpadla"), () -> FireplaceCircularPump.getState());
        StatsCollector.getInstance().collect("Krb chod čerpadla", FireplaceCircularPump.getState(), countStats -> FireplaceCircularPump.addSubscriber(countStats));
    }

    private static void configureServiceMode() {
        ServiceMode.addInput(new NamedPort(STRONG_WIND_PIN_REF_CD, "Silný vietor"), () -> Main.pinManager.getInput(STRONG_WIND_PIN_REF_CD).isLow());
        ServiceMode.addInput(new NamedPort(DAY_LIGHT_PIN_REF_CD, "Jas"), () -> DayLight.inst().getCurrentState());
        ServiceMode.addInput(new NamedPort("DAY_LIGHT_LIMIT", "Jas - limit splnený"), () -> DayLight.inst().enoughLight());
        ServiceMode.addInput(new NamedPort(HEATER_CIRCULAR_REF_CD, "Kúrenie chod čerpadla"), () -> HeaterCircularPump.getState());
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.pinManager.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.pinManager.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_BLOCK_PIN, "Blokovanie ohrevu TA3"), key -> Main.pinManager.getOutput(key));
        ServiceMode.addInput(new NamedPort(Flame.HEATER_FLAME_REF_CD, "Horák plynového kotla"), () -> Flame.getState());
        ServiceMode.addInput(new NamedPort(Boiler.HEATER_BOILER_FEC_CD, "Ohrev TA3 plynovým kotlom"), () -> Boiler.getState());
    }

    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - obehové čerpadlo", Main.pinManager.getOutput(CIRCULAR_PUMP_PORT));
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
        StatsCollector.getInstance().collect("Blokovanie ohrevu TA3", Main.pinManager.getOutput(BOILER_BLOCK_PIN));
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
