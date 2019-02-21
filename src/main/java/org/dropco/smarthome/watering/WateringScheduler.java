package org.dropco.smarthome.watering;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.solar.move.SafetySolarPanel;

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

    public WateringScheduler(WateringDao wateringDao) {
        this.wateringDao = wateringDao;
    }

    public void schedule(SafetySolarPanel safetySolarPanel) {
        ScheduledExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
        Calendar calendar = Calendar.getInstance();
        List<WateringRecord> todayRecords = wateringDao.getTodayRecords(calendar);
        for (WateringRecord record : todayRecords) {
            long delay = millisRemaining(Calendar.getInstance(), record.getMonth(), record.getDay(), record.getHour(), record.getMinute());
            logger.log(Level.INFO, "Scheduling "+record + " with delay of "+ delay);
            executorService.schedule(new WateringScheduledWork(record.getZoneRefCode(),record.getTimeInSeconds()),delay,TimeUnit.MILLISECONDS);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long delay = millisRemaining(Calendar.getInstance(), calendar.get(Calendar.MONTH)+1, calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        logger.log(Level.INFO, "Scheduling for next day is delayed for "+ delay);
        executorService.schedule(() -> schedule(safetySolarPanel), delay, TimeUnit.MILLISECONDS);

    }

    static long millisRemaining(Calendar current, int month, int day, int hour, int minute) {
        Date currentDate = current.getTime();
        if ((month - 1) < current.get(Calendar.MONTH)) {
            current.add(Calendar.YEAR, 1);
        }
        current.set(Calendar.MONTH, month - 1);
        current.set(Calendar.DAY_OF_MONTH, day);
        current.set(Calendar.HOUR_OF_DAY, hour);
        current.set(Calendar.MINUTE, minute);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        long diff = future.getTime() - currentDate.getTime();
        return diff;
    }
}