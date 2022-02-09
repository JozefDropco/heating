package org.dropco.smarthome;

import com.pi4j.io.gpio.GpioFactory;

import java.util.concurrent.TimeUnit;

public class LogCleanup {


    public void start(){
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 20, TimeUnit.SECONDS);
    }

    void runCleanUp(){

        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 1, TimeUnit.HOURS);
    }
}
