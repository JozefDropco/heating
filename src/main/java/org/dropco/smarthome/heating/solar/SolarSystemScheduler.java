package org.dropco.smarthome.heating.solar;

import java.util.logging.Logger;

public class SolarSystemScheduler {
    private static Logger logger = Logger.getLogger(SolarSystemScheduler.class.getName());

//
//    public void moveToLastPosition(SafetySolarPanel safetySolarPanel){
//        AbsolutePosition position = Db.applyDao(new SolarSystemDao(),SolarSystemDao::getNormalPosition);
//        if (position!=null) {
//            logger.log(Level.INFO, "Posun na poslednú pozíciu podľa rozvruhu");
//            logger.log(Level.FINE, "Posledná pozícia" + position);
//            new Thread(new SolarSystemScheduledWork(safetySolarPanel, true, position)).start();
//        }
//    }
//    public void schedule(SafetySolarPanel safetySolarPanel) {
//        ScheduledExecutorService executorService = GpioFactory.getExecutorServiceFactory().getScheduledExecutorService();
//        Calendar calendar = Calendar.getInstance();
//        List<SolarPanelStepRecord> todayRecords = Db.applyDao(new SolarSystemDao(), dao-> dao.getForMonth(calendar)).getRemainingSteps();
//        for (SolarPanelStepRecord record : todayRecords) {
//            long delay = millisRemaining(Calendar.getInstance(), record.getHour(), record.getMinute());
//            logger.log(Level.FINE, "Scheduling "+record + " with delay of "+ delay);
//            executorService.schedule(new SolarSystemScheduledWork(safetySolarPanel,record.getIgnoreDayLight(), record.getPosition()), delay, TimeUnit.MILLISECONDS);
//        }
//        calendar.set(Calendar.HOUR_OF_DAY, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
//        calendar.add(Calendar.DAY_OF_YEAR, 1);
//        long delay = millisRemaining(Calendar.getInstance(), 23, 59) + 65 * 1000;
//        logger.log(Level.FINE, "Scheduling for next day is delayed for "+ delay);
//        executorService.schedule(() -> {
//            DayLight.inst().clear();
//            schedule(safetySolarPanel);
//        }, delay, TimeUnit.MILLISECONDS);
//
//    }
//
//    static long millisRemaining(Calendar current, int hour, int minute) {
//        Date currentDate = current.getTime();
//        current.set(Calendar.HOUR_OF_DAY, hour);
//        current.set(Calendar.MINUTE, minute);
//        current.set(Calendar.SECOND, 0);
//        current.set(Calendar.MILLISECOND, 0);
//        Date future = current.getTime();
//        return future.getTime() - currentDate.getTime();
//    }
}
