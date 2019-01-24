package org.dropco.smarthome.solar;

import com.pi4j.io.gpio.GpioFactory;
import org.dropco.smarthome.database.SolarSystemDao;
import org.dropco.smarthome.solar.move.SafetySolarPanel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SolarSystemScheduler {
    private SolarSystemDao solarSystemDao;

    public SolarSystemScheduler(SolarSystemDao solarSystemDao) {
        this.solarSystemDao = solarSystemDao;
    }

    public void schedule(SafetySolarPanel safetySolarPanel) {
        ScheduledExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
        Calendar calendar = Calendar.getInstance();
        List<SolarPanelStepRecord> todayRecords = solarSystemDao.getTodayRecords(calendar);
        for (SolarPanelStepRecord record : todayRecords) {
            Calendar tmpCal = Calendar.getInstance(calendar.getTimeZone());
            tmpCal.setTime(calendar.getTime());
            executorService.schedule(new SolarSystemScheduledWork(safetySolarPanel, record.getPanelPosition()), millisRemaining(tmpCal, record.getMonth(), record.getDay(), record.getHour(), record.getMinute()), TimeUnit.MILLISECONDS);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        executorService.schedule(() -> schedule(safetySolarPanel), millisRemaining(Calendar.getInstance(), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE)), TimeUnit.MILLISECONDS);

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