package org.dropco.smarthome.watering;

import com.google.common.collect.FluentIterable;
import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.watering.db.WateringDao;
import org.dropco.smarthome.watering.db.WateringRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WateringScheduler {
    private static Logger logger = Logger.getLogger(WateringScheduler.class.getName());
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private WateringDao wateringDao;
    public static final int ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
    private static final ScheduledExecutorService EXECUTOR_SERVICE = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();

    public WateringScheduler(WateringDao wateringDao) {
        this.wateringDao = wateringDao;
    }

    public void schedule() {
        Calendar calendar = Calendar.getInstance();
        List<WateringRecord> allWaterings = wateringDao.getActiveRecords();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        FluentIterable<WateringRecord> todays = FluentIterable.from(allWaterings).filter(rec -> (dayOfYear % rec.getModulo()) == rec.getReminder());
        for (WateringRecord record : todays) {
            schedule(record);
        }
        long delay = ONE_DAY_IN_MILLIS - Math.abs(millisRemaining(Calendar.getInstance(), 0, 0));
        EXECUTOR_SERVICE.schedule(() -> schedule(), delay, TimeUnit.MILLISECONDS);

    }

    public static void schedule(WateringRecord record) {
        long delay = millisRemaining(Calendar.getInstance(), record.getHour(), record.getMinute());
        if (delay>=0) {
           logger.log(Level.INFO, "Polievanie zóny " + record.getName() + " začne o " + format.format(getDate(Calendar.getInstance(),record.getHour(), record.getMinute())));
           EXECUTOR_SERVICE.schedule(new WateringScheduledWork(record), delay, TimeUnit.MILLISECONDS);
        }
    }

    static long millisRemaining(Calendar current, int hour, int minute) {
        Date currentDate = current.getTime();
        Date future = getDate(current, hour, minute);
        return future.getTime() - currentDate.getTime();
    }

    private static Date getDate(Calendar current, int hour, int minute) {
        current.set(Calendar.HOUR_OF_DAY, hour);
        current.set(Calendar.MINUTE, minute);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        return current.getTime();
    }
}
