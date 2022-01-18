package org.dropco.smarthome.heating.db;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.querydsl.SolarMove;
import org.dropco.smarthome.dto.LongConstant;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.dropco.smarthome.heating.solar.dto.*;

import java.sql.Connection;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntBinaryOperator;

import static org.dropco.smarthome.database.querydsl.SolarMove.SOLAR_MOVE;
import static org.dropco.smarthome.database.querydsl.SolarPanelSchedule.SOLAR_SCHEDULE;

public class SolarSystemDao implements Dao {
    protected static final String SOLAR_PANEL_DELAY = "SOLAR_PANEL_DELAY";
    protected static final SQLTemplates DEFAULT = new MySQLTemplates();
    private SettingsDao settingsDao = new SettingsDao();
    private static LongConstant HORIZONTAL = (LongConstant) new LongConstant().setRefCd(SolarSystemRefCode.LAST_KNOWN_POSITION_HORIZONTAL).setDescription("Posledná známa horizontálna pozícia")
            .setConstantType("number").setGroup("Kolektory").setValueType("number");
    private static LongConstant VERTICAL = (LongConstant) new LongConstant().setRefCd(SolarSystemRefCode.LAST_KNOWN_POSITION_VERTICAL).setDescription("Posledná známa vertikálna pozícia")
            .setConstantType("number").setGroup("Kolektory").setValueType("number");


    private static final AtomicBoolean loaded = new AtomicBoolean();
    private static final AtomicInteger vertical = new AtomicInteger();
    private static final AtomicInteger horizontal = new AtomicInteger();
    private static final AtomicInteger NORTH = new AtomicInteger();
    private static final AtomicInteger SOUTH = new AtomicInteger();
    private static final AtomicInteger EAST = new AtomicInteger();
    private static final AtomicInteger WEST = new AtomicInteger();

    private static final AtomicInteger countDown = new AtomicInteger(30);
    private static final Lock lock = new ReentrantLock();

    private Connection connection;

    public SolarSystemDao() {
    }

    public AbsolutePosition getLastKnownPosition() {
        lock.lock();
        try {
            if (!loaded.get()){
                vertical.set((int) settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_VERTICAL));
                horizontal.set((int) settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_HORIZONTAL));
                WEST.set((int) settingsDao.getLong("WEST"));
                EAST.set((int) settingsDao.getLong("EAST"));
                NORTH.set((int) settingsDao.getLong("NORTH"));
                SOUTH.set((int) settingsDao.getLong("SOUTH"));
                loaded.set(true);
            }
            return new AbsolutePosition(horizontal.get(),vertical.get());
        } finally {
            lock.unlock();
        }
    }


    public void updateLastKnownVerticalPosition(int tick) {
        lock.lock();
        int current;
        try {
            vertical.accumulateAndGet(tick, (left, right) -> {
                int value = left + right;
                int min = Math.min(NORTH.get(), SOUTH.get());
                int max = Math.max(NORTH.get(), SOUTH.get());
                if (value< min) return min;
                if (value> max) return max;
                return value;
            });
            current = countDown.decrementAndGet();
            if (current == 0) {
                countDown.set(30);
                settingsDao.updateLongConstant(HORIZONTAL.setValue((long) horizontal.get()));
                settingsDao.updateLongConstant(VERTICAL.setValue((long) vertical.get()));
            }
        } finally {
            lock.unlock();
        }
    }


    public void updateLastKnownHorizontalPosition(int tick) {
        lock.lock();
        int current;
        try {
            horizontal.accumulateAndGet(tick, (left, right) -> {
                int value = left + right;
                int min = Math.min(WEST.get(), EAST.get());
                int max = Math.max(WEST.get(), EAST.get());
                if (value< min) return min;
                if (value> max) return max;
                return value;
            });
            current = countDown.decrementAndGet();
            if (current == 0) {
                countDown.set(30);
                settingsDao.updateLongConstant(HORIZONTAL.setValue((long) horizontal.get()));
                settingsDao.updateLongConstant(VERTICAL.setValue((long) vertical.get()));
            }
        } finally {
            lock.unlock();
        }
    }

    public void flushPosition(){
        lock.lock();
        try {
            settingsDao.updateLongConstant(HORIZONTAL.setValue((long) horizontal.get()));
            settingsDao.updateLongConstant(VERTICAL.setValue((long) vertical.get()));
        } finally {
            lock.unlock();
        }
    }

    public SolarSchedule getTodaysSchedule(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH) + 1;
        Tuple sTuple = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_SCHEDULE.all()).from(SOLAR_SCHEDULE).where(SOLAR_SCHEDULE.month.eq(month)).fetchFirst();
        SolarSchedule schedule = new SolarSchedule();
        schedule.setMonth(month);
        schedule.setHorizontalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.horizontalStep));
        schedule.setVerticalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.verticalStep));
        schedule.setSteps(Lists.newArrayList());
        SolarPanelStep sunRise = new SolarPanelStep();
        sunRise.setHour(sTuple.get(SOLAR_SCHEDULE.sunRiseHour));
        sunRise.setMinute(sTuple.get(SOLAR_SCHEDULE.sunRiseMinute));
        sunRise.setPosition(ParkPosition.INSTANCE);
        sunRise.setIgnoreDayLight(false);
        schedule.getSteps().add(sunRise);

        List<Tuple> lst = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_MOVE.all()).from(SOLAR_MOVE)
                .where(SOLAR_MOVE.month.eq(month)).orderBy(SOLAR_MOVE.hour.asc(), SOLAR_MOVE.minute.asc()).fetch();
        for (Tuple tuple : lst) {
            SolarPanelStep current = toRecord(tuple, schedule);
            schedule.getSteps().add(current);
        }
        SolarPanelStep sunSet = new SolarPanelStep();
        sunSet.setHour(sTuple.get(SOLAR_SCHEDULE.sunSetHour));
        sunSet.setMinute(sTuple.get(SOLAR_SCHEDULE.sunSetMinute));
        sunSet.setPosition(new AbsolutePosition(sTuple.get(SOLAR_SCHEDULE.sunSetAbsPosHor), sTuple.get(SOLAR_SCHEDULE.sunSetAbsPosVert)));
        sunSet.setIgnoreDayLight(true);
        schedule.getSteps().add(sunSet);
        return schedule;
    }


    private SolarPanelStep toRecord(Tuple tuple, SolarSchedule schedule) {
        SolarPanelStep record = new SolarPanelStep();
        record.setHour(tuple.get(SOLAR_MOVE.hour));
        record.setMinute(tuple.get(SOLAR_MOVE.minute));
        record.setPosition(new DeltaPosition(tuple.get(SOLAR_MOVE.horizontalSteps) * schedule.getHorizontalTickCountForStep(), tuple.get(SOLAR_MOVE.verticalSteps) * schedule.getVerticalTickCountForStep()));
        return record;
    }

    private Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        settingsDao.setConnection(connection);
    }

    public long getDelay() {
        return settingsDao.getLong(SOLAR_PANEL_DELAY);
    }

    public DeltaPosition getStrongWindPosition() {
        SolarSchedule month = getTodaysSchedule(Calendar.getInstance());
        return new DeltaPosition(0, month.getVerticalTickCountForStep() * -2);
    }


}
