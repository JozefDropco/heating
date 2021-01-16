package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.dropco.smarthome.heating.BoilerBlocker.BOILER_PORT_KEY;
import static org.dropco.smarthome.heating.CircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.ThreeWayValve.THREE_WAY_PORT;

public class HeatingWorker  {

    public static void start(SettingsDao settingsDao,BiConsumer<String, Boolean> commandExecutor) {
        new Thread(new CircularPump(settingsDao,commandExecutor)).start();
        new Thread(new ThreeWayValve(settingsDao,commandExecutor)).start();
        new Thread(new BoilerBlocker(commandExecutor)).start();
        configureServiceMode();
        addToStats();
    }

    private static void configureServiceMode() {
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_PORT_KEY, "Blokovanie ohrevu TA3"), key -> Main.getOutput(key));
    }
    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - obehové čerpadlo",Main.getOutput(CIRCULAR_PUMP_PORT));
        StatsCollector.getInstance().collect("3-cestný ventil - Bypass", !ThreeWayValve.getState() && CircularPump.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> addToStats) {
                ThreeWayValve.addSubscriber(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean valveOpened) {
                        //valveShould be closed and pump should be running to add this to Stats otherwise we shouldnt count it to stats
                        addToStats.accept(!valveOpened && CircularPump.getState());
                    }
                });
                CircularPump.addSubscriber(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean pumpRunning) {
                        //if Pump is running we should count it to stats only if valve is closed
                        addToStats.accept(pumpRunning && !ThreeWayValve.getState());

                    }
                });
            }
        });
        StatsCollector.getInstance().collect("3-cestný ventil - Ohrev", ThreeWayValve.getState() && CircularPump.getState(), new Consumer<Consumer<Boolean>>() {
            @Override
            public void accept(Consumer<Boolean> addToStats) {
                ThreeWayValve.addSubscriber(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean valveOpened) {
                        //valveShould be opened and pump should be running to add this to Stats otherwise we shouldnt count it to stats
                        addToStats.accept(valveOpened && CircularPump.getState());
                    }
                });
                CircularPump.addSubscriber(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean pumpRunning) {
                        //if Pump is running we should count it to stats only if valve is opened
                        addToStats.accept(pumpRunning && ThreeWayValve.getState());

                    }
                });

            }
        });
        StatsCollector.getInstance().collect("Blokovanie ohrevu TA3", Main.getOutput(BOILER_PORT_KEY));
    }
}
