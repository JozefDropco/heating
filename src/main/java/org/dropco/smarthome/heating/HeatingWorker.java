package org.dropco.smarthome.heating;

import com.pi4j.io.gpio.PinState;
import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.solar.SolarSystemRefCode;
import org.dropco.smarthome.stats.StatsCollector;

import java.util.function.BiConsumer;

import static org.dropco.smarthome.heating.Boiler.BOILER_PORT_KEY;
import static org.dropco.smarthome.heating.Boiler.state;
import static org.dropco.smarthome.heating.CircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.ThreeWayValve.THREE_WAY_PORT;

public class HeatingWorker  {

    public static void start(SettingsDao settingsDao,BiConsumer<String, Boolean> commandExecutor) {
        new Thread(new CircularPump(settingsDao,commandExecutor)).start();
        new Thread(new ThreeWayValve(settingsDao,commandExecutor)).start();
        new Thread(new Boiler(commandExecutor)).start();
        configureServiceMode();
        addToStats();
    }

    private static void configureServiceMode() {
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Kolektory - obehové čerpadlo"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_PORT_KEY, "Ohrev TA3"), key -> Main.getOutput(key));
    }
    private static void addToStats() {
        StatsCollector.getInstance().collect("Kolektory - obehové čerpadlo",Main.getOutput(CIRCULAR_PUMP_PORT));
        StatsCollector.getInstance().collect("3-cestný ventil - Bypass", Main.getOutput(THREE_WAY_PORT), PinState.LOW, state -> CircularPump.getState());
        StatsCollector.getInstance().collect("3-cestný ventil - Ohrev", Main.getOutput(THREE_WAY_PORT), PinState.HIGH, state -> CircularPump.getState());
        StatsCollector.getInstance().collect("Ohrev TA3", Main.getOutput(BOILER_PORT_KEY));
    }
}
