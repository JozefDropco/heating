package org.dropco.smarthome.heating.solar;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.heating.solar.move.HorizontalMoveFeedback;
import org.dropco.smarthome.heating.solar.move.VerticalMoveFeedback;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.BiConsumer;

import static org.dropco.smarthome.heating.solar.BoilerBlocker.BOILER_BLOCK_PIN;
import static org.dropco.smarthome.heating.solar.SolarCircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.solar.ThreeWayValve.THREE_WAY_PORT;

public class SolarHeatingMain {


    public static void start(BiConsumer<String, Boolean> commandExecutor) {
        SolarHeatingCurrentSetup.start();
        new Thread(new SolarCircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new BoilerBlocker(commandExecutor)).start();
        configureServiceMode();
        addToStats();
    }

    private static void configureServiceMode() {
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_BLOCK_PIN, "Blokovanie ohrevu TA3"), key -> Main.getOutput(key));
    }
    private static void addToStats() {
        StatsCollector.getInstance().collect("S-J indikator", true,VerticalMoveFeedback.getInstance()::addRealTimeTicker);
        StatsCollector.getInstance().collect("V-Z indikator", true, HorizontalMoveFeedback.getInstance()::addRealTimeTicker);

        StatsCollector.getInstance().collect("Kolektory - obehové čerpadlo",Main.getOutput(CIRCULAR_PUMP_PORT));
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
    }
}
