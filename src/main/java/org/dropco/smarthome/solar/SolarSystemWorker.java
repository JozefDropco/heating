package org.dropco.smarthome.solar;

import org.dropco.smarthome.database.SolarSystemDao;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SolarSystemWorker implements Runnable {

    private final SolarSystemDao dao;
    private SolarPanel solarPanel;
    private AtomicBoolean shutdownRequested;
    private AtomicBoolean strongWind;
    private AtomicBoolean overheated;


    public SolarSystemWorker(AtomicBoolean shutdownRequested, AtomicBoolean strongWind, AtomicBoolean overheated, SolarSystemDao dao, SolarPanel solarPanel) {
        this.strongWind = strongWind;
        this.overheated = overheated;
        this.dao = dao;
        this.shutdownRequested = shutdownRequested;
        this.solarPanel = solarPanel;
        solarPanel.setShouldTerminate(() -> strongWind.get() || overheated.get());
    }

    public void run() {
        while (!shutdownRequested.get()) {
            try {
                Calendar calendar = getCalendar();
                SolarPanelStepRecord nextRecord = dao.getNextRecord(calendar, solarPanel.getCurrentPosition());
                SolarPanelStepRecord futurePosition = null;
                if (!strongWind.get() && !overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get() || overheated.get());
                    solarPanel.move(nextRecord.getPanelPosition());
                    futurePosition = dao.getNextRecord(calendar, nextRecord.getPanelPosition());
                }
                if (strongWind.get()) {
                    solarPanel.setShouldTerminate(() -> this.overheated.get());
                    solarPanel.move(dao.getStrongWindPosition());
                    futurePosition = nextRecord;
                }
                if (overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get());
                    solarPanel.move(dao.getOverheatedPosition());
                    futurePosition = nextRecord;
                }
                synchronized (Thread.currentThread()) {
                    Thread.currentThread().wait(millisRemaining(calendar, futurePosition));
                }
            } catch (InterruptedException e) {
            }
        }
    }

    Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT")));
    }

    static long millisRemaining(Calendar current, SolarPanelStepRecord nextRecord) {
        WeekDay weekDay = WeekDay.fromCalendarDay(current.get(Calendar.DAY_OF_WEEK));
        WeekDay futurePositionDay = nextRecord.getWeekDay();
        //1 - SUNDAY, 7 - SATURDAY
        int currentCalDay = weekDay.calendarDay;
        int futureCalendarDay = futurePositionDay.calendarDay;
        if (currentCalDay > futureCalendarDay) {
            currentCalDay += 7; //add virtually a week
        }
        Date currentDate = current.getTime();
        current.add(Calendar.DAY_OF_YEAR, (futureCalendarDay - currentCalDay));
        current.set(Calendar.HOUR_OF_DAY, nextRecord.getHour());
        current.set(Calendar.MINUTE, nextRecord.getMinute());
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        return future.getTime() - currentDate.getTime();
    }


}
