package org.dropco.smarthome.watering2;

import com.google.common.collect.FluentIterable;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.watering2.db.WateringDao;
import org.dropco.smarthome.watering2.db.WateringRecord;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WateringScheduler {
    private static Logger logger = Logger.getLogger(WateringScheduler.class.getName());
    private WateringDao wateringDao;
    public static final int ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

    public WateringScheduler(WateringDao wateringDao) {
        this.wateringDao = wateringDao;
    }

    public void schedule() {
        Calendar calendar = Calendar.getInstance();
        List<WateringRecord> allWaterings = wateringDao.getWateringRecords();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        FluentIterable<WateringRecord> todays = FluentIterable.from(allWaterings).filter(rec -> (dayOfYear % rec.getModulo()) == rec.getReminder());
        for (WateringRecord record : todays) {
            schedule(record);
        }
        long delay = ONE_DAY_IN_MILLIS - millisRemaining(Calendar.getInstance(), 0, 0);
        logger.log(Level.INFO, "Scheduling for next day is delayed for " + delay);
        EXECUTOR_SERVICE.schedule(() -> schedule(), delay, TimeUnit.MILLISECONDS);

    }

    static void schedule(WateringRecord record) {
        long delay = millisRemaining(Calendar.getInstance(), record.getHour(), record.getMinute());
        logger.log(Level.INFO, "Scheduling " + record + " with delay of " + delay);
        EXECUTOR_SERVICE.schedule(new WateringScheduledWork(record), delay, TimeUnit.MILLISECONDS);
    }

    static long millisRemaining(Calendar current, int hour, int minute) {
        Date currentDate = current.getTime();
        current.set(Calendar.HOUR_OF_DAY, hour);
        current.set(Calendar.MINUTE, minute);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        return future.getTime() - currentDate.getTime();
    }
}