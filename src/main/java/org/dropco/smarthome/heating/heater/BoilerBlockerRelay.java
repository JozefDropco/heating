package org.dropco.smarthome.heating.heater;

import com.pi4j.io.gpio.GpioFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BoilerBlockerRelay {
    public static final String BOILER_BLOCK_PIN7 = "BOILER_BLOCK_PIN7";
    public static final String BOILER_BLOCK_PIN8 = "BOILER_BLOCK_PIN8";
    private static final Logger logger = Logger.getLogger(BoilerBlockerRelay.class.getName());
    private ReentrantLock lock = new ReentrantLock(true);
    private ExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
    private BiConsumer<String, Boolean> commandExecutor;

    public BoilerBlockerRelay(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
        commandExecutor.accept(BOILER_BLOCK_PIN7,false);
    }


    public void startBlocking() {
        executorService.submit(() -> {
            lock.lock();
            try {
                commandExecutor.accept(BOILER_BLOCK_PIN7, true);
                logger.log(Level.INFO, "Nastavujem PIN7 na 1");
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, false);
                logger.log(Level.INFO, "Nastavujem PIN7 na 0");

            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Sleep interrupted");
            } finally {
                lock.unlock();
            }
        });
    }

    public void stopBlocking() {
        executorService.submit(() -> {
            lock.lock();
            try {
                commandExecutor.accept(BOILER_BLOCK_PIN8, true);
                logger.log(Level.INFO, "Nastavujem PIN8 na 1");
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, true);
                logger.log(Level.INFO, "Nastavujem PIN7na 1");
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, false);
                logger.log(Level.INFO, "Nastavujem PIN7 na 0");
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN8, false);
                logger.log(Level.INFO, "Nastavujem PIN8 na 0");
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Sleep interrupted");
            } finally {
                lock.unlock();
            }
        });
    }
}
