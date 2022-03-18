package org.dropco.smarthome.temp;

import com.pi4j.io.gpio.GpioFactory;
import com.querydsl.core.Tuple;
import org.dropco.smarthome.database.Db;
import org.dropco.smarthome.database.LogDao;
import org.dropco.smarthome.database.SettingsDao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeriodicCleanup {


    public void start(){
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 1, TimeUnit.MINUTES);
    }

    void runCleanUp(){
        try {
            Date date = Db.applyDao(new LogDao(), dao -> dao.retrieveLastDay());
            if (date != null) {
                Calendar lastDay = Calendar.getInstance();
                lastDay.setTime(date);
                Date currentDate = new Date();
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.setTime(currentDate);
                currentCalendar.set(Calendar.MINUTE, 0);
                currentCalendar.set(Calendar.SECOND, 0);
                currentCalendar.set(Calendar.MILLISECOND, 0);
                while (lastDay.getTime().before(currentDate)) {
                    Db.acceptDao(new LogDao(), dao -> {
                        Iterable<LogDao.HourAggregatedTemp> forDay = dao.retrieveForDay(lastDay.getTime());
                        for (LogDao.HourAggregatedTemp record : forDay) {
                            if (record.asOf.before(currentCalendar.getTime())) {
                                dao.moveToHistory(record);
                            }
                        }
                    });
                    Db.acceptDao(new LogDao(), dao -> {
//                        dao.deleteTempData(lastDay.getTime());
                    });
                    lastDay.add(Calendar.DAY_OF_YEAR, 1);
                }

            }
        } catch (Exception e){
            Logger.getLogger(PeriodicCleanup.class.getName()).log(Level.SEVERE,"Periodické čistenie teplôt zlyhalo", e);
        }
        GpioFactory.getExecutorServiceFactory().getScheduledExecutorService().schedule(this::runCleanUp, 1, TimeUnit.HOURS);
    }
}
