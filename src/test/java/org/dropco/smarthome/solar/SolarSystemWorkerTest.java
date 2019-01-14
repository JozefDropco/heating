package org.dropco.smarthome.solar;

import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.SolarSystemDao;
import org.junit.Assert;
import org.junit.Test;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

public class SolarSystemWorkerTest {

    @Test
    public void run() {
        AtomicBoolean shutdownRequested = new AtomicBoolean(false);
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean overheated = new AtomicBoolean(false);
        SolarPanel solarPanel = solarPanel(new ArrayList<>());
        SolarSystemWorker worker = new SolarSystemWorker(shutdownRequested, strongWind, overheated, new SolarSystemDao(new SettingsDao()) {
            @Override
            public SolarPanelStepRecord getRecentRecord(Calendar calendar) {
                return getNextRecord(calendar);
            }

            @Override
            public SolarPanelStepRecord getNextRecord(Calendar calendar) {
                SolarPanelStepRecord stepRecord = startPos();
                stepRecord.setHour(7);
                stepRecord.getPanelPosition().setHorizontalPositionInSeconds(-10);
                return stepRecord;
            }
        }, solarPanel);
        Thread thread = new Thread(worker);
        thread.start();
        shutdownRequested.set(true);
        synchronized (thread) {
            thread.notify();
        }

    }

    @Test
    public void runStrongWind() throws InterruptedException {
        AtomicBoolean shutdownRequested = new AtomicBoolean(false);
        AtomicBoolean strongWind = new AtomicBoolean(false);
        AtomicBoolean overheated = new AtomicBoolean(false);
        ArrayList<String> list = new ArrayList<>();
        SolarPanel solarPanel = solarPanel(list);
        solarPanel.getCurrentPosition().setHorizontalPositionInSeconds(-15);
        solarPanel.getCurrentPosition().setVerticalPositionInSeconds(-30);
        SolarSystemWorker worker = new SolarSystemWorker(shutdownRequested, strongWind, overheated, new SolarSystemDao(new SettingsDao()) {

            @Override
            public SolarPanelStepRecord getRecentRecord(Calendar calendar) {
                return getNextRecord(calendar);
            }

            @Override
            public SolarPanelStepRecord getNextRecord(Calendar calendar) {
                SolarPanelStepRecord stepRecord = startPos();
                stepRecord.setHour(7);
                stepRecord.getPanelPosition().setHorizontalPositionInSeconds(-20);
                stepRecord.getPanelPosition().setVerticalPositionInSeconds(-30);
                return stepRecord;
            }

            @Override
            public SolarPanelPosition getStrongWindPosition() {
                SolarPanelPosition solarPanelPosition = new SolarPanelPosition();
                solarPanelPosition.setHorizontalPositionInSeconds(0);
                return solarPanelPosition;
            }
        }, solarPanel) {
            @Override
            Calendar getCalendar() {
                Calendar instance = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT")));
                instance.set(2018, 11, 31, 6, 10, 00);
                instance.set(Calendar.HOUR_OF_DAY, 6);
                return instance;
            }
        };
        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(10 * 1000);
        strongWind.set(true);
        synchronized (thread) {
            thread.notify();
        }
        Thread.sleep(40 * 1000);
        shutdownRequested.set(true);
        synchronized (thread) {
            thread.notify();
        }
        Thread.sleep(5000);
        Assert.assertFalse(thread.isAlive());
        Assert.assertEquals(0, solarPanel.getCurrentPosition().getHorizontalPositionInSeconds());
        Assert.assertEquals(-30, solarPanel.getCurrentPosition().getVerticalPositionInSeconds());
        Assert.assertTrue(list.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + true));
        Assert.assertTrue(list.remove(SolarSystemRefCode.SOUTH_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.WEST_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.EAST_PIN_REF_CD + false));
        Assert.assertTrue(list.remove(SolarSystemRefCode.NORTH_PIN_REF_CD + true));
    }

    @Test
    public void millisRemainingNextDay() {
        Calendar current = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT")));
        current.set(2018, 11, 30, 18, 0, 0);
        current.set(Calendar.MILLISECOND, 0);
        long remaining = SolarSystemWorker.millisRemaining(current, position(-5, 0, 6, 10, 12, 1));
        Assert.assertEquals((12 * 60 + 10) * 60 * 1000l, remaining);
    }


    @Test
    public void millisRemainingNext3Days() {
        Calendar current = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("GMT")));
        current.set(2018, 11, 30, 18, 0, 0);
        current.set(Calendar.MILLISECOND, 0);
        long remaining = SolarSystemWorker.millisRemaining(current, position(-5, 0, 6, 10, 12, 3));
        Assert.assertEquals((2 * 24 * 60 + 12 * 60 + 10) * 60 * 1000l, remaining);
    }


    private SolarPanel solarPanel(List<String> result) {
        return new SolarPanel(startPos().getPanelPosition(), (s, aBoolean) -> {
            result.add(s + aBoolean);
        });
    }

    private SolarPanelStepRecord startPos() {
        return position(0, 0, 6, 10, 12, 1);
    }

    private SolarPanelStepRecord position(int horizontalPositionInSeconds, int verticalPositionInSeconds, int hour, int minute, int month, int day) {
        SolarPanelStepRecord currentPosition = new SolarPanelStepRecord();
        SolarPanelPosition position = new SolarPanelPosition();
        position.setHorizontalPositionInSeconds(horizontalPositionInSeconds);
        position.setVerticalPositionInSeconds(verticalPositionInSeconds);
        currentPosition.setPanelPosition(position);
        currentPosition.setHour(hour);
        currentPosition.setMinute(minute);
        currentPosition.setMonth(month);
        currentPosition.setDay(day);
        return currentPosition;
    }
}