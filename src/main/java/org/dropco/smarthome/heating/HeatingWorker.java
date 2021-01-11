package org.dropco.smarthome.heating;

import org.dropco.smarthome.Main;
import org.dropco.smarthome.ServiceMode;
import org.dropco.smarthome.dto.NamedPort;
import org.dropco.smarthome.gpioextension.ExtendedPin;
import org.dropco.smarthome.solar.SolarSystemRefCode;

import java.util.function.BiConsumer;

import static org.dropco.smarthome.heating.Boiler.BOILER_PORT_KEY;
import static org.dropco.smarthome.heating.CircularPump.CIRCULAR_PUMP_PORT;
import static org.dropco.smarthome.heating.ThreeWayValve.THREE_WAY_PORT;

public class HeatingWorker  {

    public static void start(BiConsumer<String, Boolean> commandExecutor) {
        new Thread(new CircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new Boiler(commandExecutor)).start();
        ServiceMode.addOutput(new NamedPort(CIRCULAR_PUMP_PORT, "Obehové čerpadlo"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(THREE_WAY_PORT, "3-cestný ventil"), key -> Main.getOutput(key));
        ServiceMode.addOutput(new NamedPort(BOILER_PORT_KEY, "Ohrev TA3"), key -> Main.getOutput(key));
    }
}
