package org.dropco.smarthome.solar;

import org.dropco.smarthome.database.SolarSystemDao;

import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SolarSystemWorker implements Runnable {

    private final SolarSystemDao dao;
    private SolarPanel solarPanel;
    private AtomicBoolean shutdownRequested;
    private AtomicBoolean strongWind;
    private AtomicBoolean overheated;
    private static final Logger logger = Logger.getLogger(SolarSystemWorker.class.getName());


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
                logger.log(Level.INFO, "Next position: " + nextRecord);
                SolarPanelStepRecord futurePosition = null;
                if (!strongWind.get() && !overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get() || overheated.get());
                    logger.log(Level.INFO, "Moving towards (strong wind): " + nextRecord.getPanelPosition());
                    solarPanel.move(nextRecord.getPanelPosition());
                    futurePosition = dao.getNextRecord(calendar, nextRecord.getPanelPosition());
                }
                if (strongWind.get()) {
                    solarPanel.setShouldTerminate(() -> this.overheated.get());
                    SolarPanelPosition strongWindPosition = dao.getStrongWindPosition();
                    logger.log(Level.INFO, "Moving towards (strong wind): " + strongWindPosition);
                    solarPanel.move(strongWindPosition);
                    futurePosition = nextRecord;
                }
                if (overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get());
                    SolarPanelPosition overheatedPosition = dao.getOverheatedPosition();
                    logger.log(Level.INFO, "Moving towards (overheated): " + overheatedPosition);
                    solarPanel.move(overheatedPosition);
                    futurePosition = nextRecord;
                }
                synchronized (Thread.currentThread()) {
                    long timeout = millisRemaining(calendar, futurePosition);
                    logger.log(Level.INFO, "Solar system will sleep for next: " + timeout + " milliseconds");
                    Thread.currentThread().wait(timeout);
                }
            } catch (InterruptedException e) {
            }
        }
    }

    Calendar getCalendar() {
        return Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT")));
    }

    static long millisRemaining(Calendar current, SolarPanelStepRecord nextRecord) {
        Date currentDate = current.getTime();
        current.set(Calendar.DAY_OF_MONTH, nextRecord.getDay());
        current.set(Calendar.HOUR_OF_DAY, nextRecord.getHour());
        current.set(Calendar.MINUTE, nextRecord.getMinute());
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        return future.getTime() - currentDate.getTime();
    }


}
