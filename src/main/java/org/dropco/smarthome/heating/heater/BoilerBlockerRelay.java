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
    private ReentrantLock lock = new ReentrantLock(true);
    private ExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
    private BiConsumer<String, Boolean> commandExecutor;

    public BoilerBlockerRelay(BiConsumer<String, Boolean> commandExecutor) {
        this.commandExecutor = commandExecutor;
    }


    public void startBlocking() {
        executorService.submit(() -> {
            lock.lock();
            try {
                commandExecutor.accept(BOILER_BLOCK_PIN7, true);
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, false);

            } catch (InterruptedException e) {
                Logger.getLogger(BoilerBlockerRelay.class.getName()).log(Level.SEVERE, "Sleep interrupted");
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
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, true);
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN7, false);
                Thread.sleep(1000);
                commandExecutor.accept(BOILER_BLOCK_PIN8, false);
            } catch (InterruptedException e) {
                Logger.getLogger(BoilerBlockerRelay.class.getName()).log(Level.SEVERE, "Sleep interrupted");
            } finally {
                lock.unlock();
            }
        });
    }
}
