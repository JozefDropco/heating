package org.dropco.smarthome;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TimerService {
    private static final Logger logger = Logger.getLogger(TimerService.class.getName());
    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);


    public static void setExecutorService(ScheduledExecutorService executorService) {
        TimerService.executorService = executorService;
    }

    public static void scheduleFor(String name, Date toDate, Runnable runnable) {
        long delay = TimeUtil.millisRemaining(Calendar.getInstance(), toDate);
        schedule(name, runnable, delay);
    }

    public static void scheduleFor(String name, int hour, int minute, Runnable runnable) {
        long delay = TimeUtil.millisRemaining(Calendar.getInstance(), hour, minute);
        schedule(name, runnable, delay);
    }

    public static void scheduleForNextDay(String name, Runnable runnable) {
        long nextDay = TimeUtil.milisRemaingForNextDay();
        schedule(name, runnable, nextDay);
    }

    public static void schedule(String name, Runnable runnable, long delay) {
        if (delay <= 0l) {
            logger.log(Level.SEVERE, "Scheduling disabled for " + name + ". Negative delay");
        } else {
            logger.log(Level.CONFIG, "Scheduling " + name + " with delay of " + delay + " ms.");
            executorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        }
    }
}
