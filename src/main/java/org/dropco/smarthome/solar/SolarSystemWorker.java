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
        logger.log(Level.INFO, "Starting solar system. Last known position to: " + solarPanel.getCurrentPosition());
        SolarPanelStepRecord nextRecord = dao.getRecentRecord(getCalendar());
        logger.log(Level.INFO, "Warm up. Move to correct location: " + nextRecord.getPanelPosition());
        while (!shutdownRequested.get()) {
            try {
                Calendar calendar = getCalendar();
                if (!strongWind.get() && !overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get() || overheated.get());
                    logger.log(Level.INFO, "Moving towards: " + nextRecord.getPanelPosition());
                    solarPanel.move(nextRecord.getPanelPosition());
                }
                if (strongWind.get()) {
                    solarPanel.setShouldTerminate(() -> this.overheated.get());
                    SolarPanelPosition strongWindPosition = dao.getStrongWindPosition();
                    logger.log(Level.INFO, "Moving towards (strong wind): " + strongWindPosition);
                    solarPanel.move(strongWindPosition);
                }
                if (overheated.get()) {
                    solarPanel.setShouldTerminate(() -> this.strongWind.get());
                    SolarPanelPosition overheatedPosition = dao.getOverheatedPosition();
                    logger.log(Level.INFO, "Moving towards (overheated): " + overheatedPosition);
                    solarPanel.move(overheatedPosition);
                }
                nextRecord = dao.getNextRecord(calendar);
                logger.log(Level.INFO, "Next position: " + nextRecord);
                synchronized (Thread.currentThread()) {
                    long timeout = millisRemaining(calendar, nextRecord);
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
        current.set(Calendar.MONTH, nextRecord.getMonth()-1);
        current.set(Calendar.DAY_OF_MONTH, nextRecord.getDay());
        current.set(Calendar.HOUR_OF_DAY, nextRecord.getHour());
        current.set(Calendar.MINUTE, nextRecord.getMinute());
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        Date future = current.getTime();
        long diff = future.getTime() - currentDate.getTime();
        return diff;
    }


}
