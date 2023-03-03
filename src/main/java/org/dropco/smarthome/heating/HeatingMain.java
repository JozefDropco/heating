package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.heater.Boiler;
import org.dropco.smarthome.heating.heater.BoilerBlocker;
import org.dropco.smarthome.heating.heater.Flame;
import org.dropco.smarthome.heating.heater.HeatingConfiguration;
import org.dropco.smarthome.heating.heater.HolidayMode;
import org.dropco.smarthome.heating.pump.FireplaceCircularPump;
import org.dropco.smarthome.heating.pump.HeaterCircularPump;
import org.dropco.smarthome.heating.pump.SolarCircularPump;
import org.dropco.smarthome.heating.solar.*;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.BiConsumer;

import static org.dropco.smarthome.heating.heater.BoilerBlocker.BOILER_BLOCK_PIN;
import static org.dropco.smarthome.heating.pump.HeaterCircularPump.HEATER_CIRCULAR_REF_CD;
import static org.dropco.smarthome.heating.pump.SolarCircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.ThreeWayValve.THREE_WAY_PORT;

public class HeatingMain {

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
        new Thread(HolidayMode.instance()).start();
        addFireplace();
        configureServiceMode();
        addToStats();
        SolarMain.start(settingsDao);
    }

    private static void addFireplace() {
        new FireplaceCircularPump(Main.pinManager.getInput(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD)).start();
        ServiceMode.addInput(new NamedPort(FireplaceCircularPump.FIREPLACE_CIRCULAR_PUMP_REF_CD, "Krb chod čerpadla"), () -> FireplaceCircularPump.getState());
        StatsCollector.getInstance().collect("Krb chod čerpadla", FireplaceCircularPump.getState(), countStats -> FireplaceCircularPump.addSubscriber(countStats));
    }

    private static void configureServiceMode() {
        ServiceMode.addInput(new NamedPort(HEATER_CIRCULAR_REF_CD, "Kúrenie chod čerpadla"), () -> HeaterCircularPump.getState());
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.pinManager.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.pinManager.getOutput(key));
//        ServiceMode.addOutput(new NamedPort(BOILER_BLOCK_PIN, "Blokovanie ohrevu TA3"), key -> BoilerBlocker.boilerBlockerRelay..pinManager.getOutput(key));
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
//        StatsCollector.getInstance().collect("Blokovanie ohrevu TA3", BoilerBlocker.Main.pinManager.getOutput(BOILER_BLOCK_PIN));
        StatsCollector.getInstance().collect("Horák plynového kotla", Flame.getState(), countStats -> Flame.addSubscriber(countStats));
        StatsCollector.getInstance().collect("Kúrenie chod čerpadla", HeaterCircularPump.getState(), countStats -> HeaterCircularPump.addSubscriber(countStats));
        StatsCollector.getInstance().collect("Ohrev TA3 plynovým kotlom", Boiler.getState(), countStats -> Boiler.addSubscriber(countStats));
    }

}
