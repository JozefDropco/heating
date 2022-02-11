package org.dropco.smarthome.heating.db;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLDeleteClause;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.querydsl.SolarMove;
import org.dropco.smarthome.dto.LongConstant;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.ParkPosition;
import org.dropco.smarthome.heating.solar.dto.SolarPanelStep;
import org.dropco.smarthome.heating.solar.dto.SolarSchedule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
        return new AbsolutePosition(horizontal.get(), vertical.get());
    }

    private void ensureLoaded() {
        lock.lock();
        try {
            if (!loaded.get()) {
                vertical.set(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_VERTICAL).intValue());
                horizontal.set(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_HORIZONTAL).intValue());
                WEST.set(settingsDao.getLong("WEST").intValue());
                EAST.set(settingsDao.getLong("EAST").intValue());
                NORTH.set(settingsDao.getLong("NORTH").intValue());
                SOUTH.set(settingsDao.getLong("SOUTH").intValue());
                loaded.set(true);
            }
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
                if (value < min) return min;
                if (value > max) return max;
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
                if (value < min) return min;
                if (value > max) return max;
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

    public void flushPosition() {
        lock.lock();
        try {
            settingsDao.updateLongConstant(HORIZONTAL.setValue((long) horizontal.get()));
            settingsDao.updateLongConstant(VERTICAL.setValue((long) vertical.get()));
        } finally {
            lock.unlock();
        }
    }

    public SolarSchedule getTodaysSchedule(int month) {
        Tuple sTuple = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_SCHEDULE.all()).from(SOLAR_SCHEDULE).where(SOLAR_SCHEDULE.month.eq(month)).fetchFirst();
        SolarSchedule schedule = new SolarSchedule();
        schedule.setMonth(month);
        schedule.setHorizontalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.horizontalStep));
        schedule.setVerticalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.verticalStep));
        schedule.setSteps(Lists.newArrayList());
        SolarPanelStep sunRise = new SolarPanelStep();
        sunRise.setHour(sTuple.get(SOLAR_SCHEDULE.sunRiseHour));
        sunRise.setMinute(sTuple.get(SOLAR_SCHEDULE.sunRiseMinute));
        sunRise.setPosition(new AbsolutePosition(EAST.get(), SOUTH.get()));
        schedule.getSteps().add(sunRise);

        List<Tuple> lst = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_MOVE.all()).from(SOLAR_MOVE)
                .where(SOLAR_MOVE.month.eq(month)).orderBy(SOLAR_MOVE.hour.asc(), SOLAR_MOVE.minute.asc()).fetch();
        for (Tuple tuple : lst) {
            SolarPanelStep current = toRecord(tuple);
            schedule.getSteps().add(current);
        }
        SolarPanelStep sunSet = new SolarPanelStep();
        sunSet.setHour(sTuple.get(SOLAR_SCHEDULE.sunSetHour));
        sunSet.setMinute(sTuple.get(SOLAR_SCHEDULE.sunSetMinute));
        sunSet.setPosition(ParkPosition.INSTANCE);
        sunSet.setIgnoreDayLight(true);
        schedule.getSteps().add(sunSet);
        return schedule;
    }


    private SolarPanelStep toRecord(Tuple tuple) {
        SolarPanelStep record = new SolarPanelStep();
        record.setHour(tuple.get(SOLAR_MOVE.hour));
        record.setMinute(tuple.get(SOLAR_MOVE.minute));
        record.setPosition(new DeltaPosition(tuple.get(SOLAR_MOVE.horizontalSteps), tuple.get(SOLAR_MOVE.verticalSteps)));
        return record;
    }

    private Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        settingsDao.setConnection(connection);
        ensureLoaded();
    }

    public long getDelay() {
        return settingsDao.getLong(SOLAR_PANEL_DELAY);
    }


    public void update(SolarSchedule solarSchedule) {
        try {
            boolean autocommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                new SQLDeleteClause(connection, MySQLTemplates.DEFAULT, SOLAR_MOVE).where(SOLAR_MOVE.month.eq(solarSchedule.getMonth())).execute();
                new SQLDeleteClause(connection, MySQLTemplates.DEFAULT, SOLAR_SCHEDULE).where(SOLAR_SCHEDULE.month.eq(solarSchedule.getMonth())).execute();
                SQLInsertClause insertClause = new SQLInsertClause(connection, MySQLTemplates.DEFAULT, SOLAR_SCHEDULE);
                insertClause.set(SOLAR_SCHEDULE.month, solarSchedule.getMonth());
                insertClause.set(SOLAR_SCHEDULE.horizontalStep, solarSchedule.getHorizontalTickCountForStep());
                insertClause.set(SOLAR_SCHEDULE.verticalStep, solarSchedule.getVerticalTickCountForStep());
                SolarPanelStep sunrise = solarSchedule.getSteps().remove(0);
                insertClause.set(SOLAR_SCHEDULE.sunRiseHour, sunrise.getHour());
                insertClause.set(SOLAR_SCHEDULE.sunRiseMinute, sunrise.getMinute());
                SolarPanelStep sunset = solarSchedule.getSteps().remove(solarSchedule.getSteps().size() - 1);
                insertClause.set(SOLAR_SCHEDULE.sunSetHour, sunset.getHour());
                insertClause.set(SOLAR_SCHEDULE.sunSetMinute, sunset.getMinute());
                insertClause.execute();
                for (SolarPanelStep step : solarSchedule.getSteps()) {
                    SQLInsertClause insert = new SQLInsertClause(connection, MySQLTemplates.DEFAULT, SOLAR_MOVE);
                    insert.set(SOLAR_MOVE.horizontalSteps, ((DeltaPosition) step.getPosition()).getHorizontalCount());
                    insert.set(SOLAR_MOVE.verticalSteps, ((DeltaPosition) step.getPosition()).getVerticalCount());
                    insert.set(SOLAR_MOVE.hour, step.getHour());
                    insert.set(SOLAR_MOVE.minute, step.getMinute());
                    insert.set(SOLAR_MOVE.month, solarSchedule.getMonth());
                    insert.execute();
                }
                connection.commit();
            } finally {
                connection.setAutoCommit(autocommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
