package org.dropco.smarthome.heating;

import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class HeatingWorker implements Runnable {

    private static BiConsumer<String, Boolean> commandExecutor;

    private static final Logger logger = Logger.getLogger(HeatingWorker.class.getName());


    @Override
    public void run() {
        new Thread(new CircularPump(commandExecutor)).start();
        new Thread(new ThreeWayValve(commandExecutor)).start();
        new Thread(new Boiler(commandExecutor)).start();
    }
}
