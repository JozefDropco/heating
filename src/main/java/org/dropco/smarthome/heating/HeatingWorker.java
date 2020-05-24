package org.dropco.smarthome.heating;

import java.util.function.BiConsumer;

public class HeatingWorker  {

    public static void start(BiConsumer<String, Boolean> commandExecutor) {
        new Thread(new CircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new Boiler(commandExecutor)).start();
    }
}
