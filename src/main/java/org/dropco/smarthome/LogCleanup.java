package org.dropco.smarthome;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.LogDao;

import java.util.concurrent.TimeUnit;

public class LogCleanup {


    public void start(){
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 20, TimeUnit.SECONDS);
    }

    void runCleanUp(){
        Db.acceptDao(new LogDao(), LogDao::deleteOldLogs);
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 1, TimeUnit.DAYS);
    }
}
