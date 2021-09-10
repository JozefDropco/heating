package org.dropco.smarthome.solar;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.querydsl.sql.MySQLTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.Dao;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.querydsl.SolarMove;
import org.dropco.smarthome.dto.LongConstant;
import org.dropco.smarthome.solar.dto.*;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.querydsl.SolarMove.SOLAR_MOVE;
import static org.dropco.smarthome.database.querydsl.SolarPanelSchedule.SOLAR_SCHEDULE;

public class SolarSystemDao implements Dao {
    protected static final String SOLAR_PANEL_DELAY = "SOLAR_PANEL_DELAY";
    protected static final SQLTemplates DEFAULT = new MySQLTemplates();
    private SettingsDao settingsDao = new SettingsDao();
    private static LongConstant HORIZONTAL = new LongConstant();
    private static LongConstant NORMAL_HORIZONTAL = new LongConstant();
    private static LongConstant VERTICAL = new LongConstant();
    private static LongConstant NORMAL_VERTICAL = new LongConstant();
    private Connection connection;

    public SolarSystemDao() {
        HORIZONTAL.setRefCd(SolarSystemRefCode.LAST_KNOWN_POSITION_HORIZONTAL).setDescription("Posledná známa horizontálna pozícia")
                .setConstantType("number").setGroup("Kolektory").setValueType("number");
        VERTICAL.setRefCd(SolarSystemRefCode.LAST_KNOWN_POSITION_VERTICAL).setDescription("Posledná známa vertikálna pozícia")
                .setConstantType("number").setGroup("Kolektory").setValueType("number");

        NORMAL_HORIZONTAL.setRefCd(SolarSystemRefCode.LAST_KNOWN_NORMAL_POSITION_HORIZONTAL).setDescription("Posledná horizontálna pozícia podľa rozvrhu")
                .setConstantType("number").setGroup("Kolektory").setValueType("number");
        NORMAL_VERTICAL.setRefCd(SolarSystemRefCode.LAST_KNOWN_NORMAL_POSITION_VERTICAL).setDescription("Posledná vertikálna pozícia podľa rozvrhu")
                .setConstantType("number").setGroup("Kolektory").setValueType("number");
    }

    public AbsolutePosition getLastKnownPosition() {
        AbsolutePosition panelPosition = new AbsolutePosition((int) settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_HORIZONTAL), (int) settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_VERTICAL));
        return panelPosition;
    }

    public void updateLastKnownPosition(AbsolutePosition currentPosition) {
        settingsDao.updateLongConstant(HORIZONTAL.setValue((long) currentPosition.getHorizontal()));
        settingsDao.updateLongConstant(VERTICAL.setValue((long) currentPosition.getVertical()));
    }


    public SolarSchedule getForMonth(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH) + 1;
        Tuple sTuple = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_SCHEDULE.all()).from(SOLAR_SCHEDULE).where(SOLAR_SCHEDULE.month.eq(month)).fetchFirst();
        SolarSchedule schedule = new SolarSchedule();
        schedule.setMonth(month);
        schedule.setHorizontalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.horizontalStep));
        schedule.setVerticalTickCountForStep(sTuple.get(SOLAR_SCHEDULE.verticalStep));

        SolarPanelStepRecord sunRise = new SolarPanelStepRecord();
        sunRise.setHour(sTuple.get(SOLAR_SCHEDULE.sunRiseHour));
        sunRise.setMinute(sTuple.get(SOLAR_SCHEDULE.sunRiseMinute));
        schedule.setCurrentNormalPosition(new AbsolutePosition(sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosHor), sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosVert)));
        sunRise.setPosition(new AbsolutePosition(sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosHor), sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosVert)));
        sunRise.setIgnoreDayLight(true);
        schedule.setSunRise(sunRise);

        SolarPanelStepRecord sunSet = new SolarPanelStepRecord();
        sunSet.setHour(sTuple.get(SOLAR_SCHEDULE.sunSetHour));
        sunSet.setMinute(sTuple.get(SOLAR_SCHEDULE.sunSetMinute));
        sunSet.setPosition(new AbsolutePosition(sTuple.get(SOLAR_SCHEDULE.sunSetAbsPosHor), sTuple.get(SOLAR_SCHEDULE.sunSetAbsPosVert)));
        sunSet.setIgnoreDayLight(true);
        schedule.setSunSet(sunSet);
        schedule.setRemainingSteps(Lists.newArrayList());
        List<Tuple> lst = new MySQLQuery<SolarMove>(getConnection()).select(SOLAR_MOVE.all()).from(SOLAR_MOVE)
                .where(SOLAR_MOVE.month.eq(month)).orderBy(SOLAR_MOVE.hour.asc(), SOLAR_MOVE.minute.asc()).fetch();
        if (isApplicable(calendar, sunRise)) schedule.getRemainingSteps().add(sunRise);
        for (Tuple tuple : lst) {
            SolarPanelStepRecord current = toRecord(tuple, schedule);
            if (isApplicable(calendar, current)) schedule.getRemainingSteps().add(current);
            else {
                current.getPosition().invoke(new PositionProcessor() {
                    @Override
                    public void process(AbsolutePosition absPos) {
                        schedule.setCurrentNormalPosition(absPos);
                    }

                    @Override
                    public void process(DeltaPosition deltaPos) {
                        AbsolutePosition currentNormalPosition = schedule.getCurrentNormalPosition();
                        currentNormalPosition.setVertical(currentNormalPosition.getVertical() + deltaPos.getDeltaVerticalTicks());
                        currentNormalPosition.setHorizontal(currentNormalPosition.getHorizontal() + deltaPos.getDeltaHorizontalTicks());
                    }
                });
            }
        }
        if (isApplicable(calendar, sunSet)) schedule.getRemainingSteps().add(sunSet);
        else schedule.setCurrentNormalPosition((AbsolutePosition) sunSet.getPosition());
        return schedule;
    }

    private boolean isApplicable(Calendar calendar, SolarPanelStepRecord record) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (currentHour < record.getHour()) return true;
        if (currentHour == record.getHour() && calendar.get(Calendar.MINUTE) <= record.getMinute()) return true;
        return false;

    }


    private SolarPanelStepRecord toRecord(Tuple tuple, SolarSchedule schedule) {
        SolarPanelStepRecord record = new SolarPanelStepRecord();
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
        SolarSchedule month = getForMonth(Calendar.getInstance());
        return new DeltaPosition(month.getHorizontalTickCountForStep() * -2, 0);
    }

    public AbsolutePosition getOverheatedPosition() {
        SolarPanelStepRecord sunRise = getForMonth(Calendar.getInstance()).getSunRise();
        return (AbsolutePosition) sunRise.getPosition();
    }

    public void saveNormalPosition(AbsolutePosition position) {
        settingsDao.updateLongConstant(NORMAL_HORIZONTAL.setValue((long) position.getHorizontal()));
        settingsDao.updateLongConstant(NORMAL_VERTICAL.setValue((long) position.getVertical()));

    }

    public AbsolutePosition getNormalPosition() {
        LongConstant horPos = settingsDao.getLongConst(NORMAL_HORIZONTAL.getRefCd()).get();
        LongConstant verPos = settingsDao.getLongConst(NORMAL_VERTICAL.getRefCd()).get();
        if (itToday(horPos.getLastModification())) {
            return new AbsolutePosition(horPos.getValue().intValue(), verPos.getValue().intValue());
        } else {
            return getForMonth(Calendar.getInstance()).getCurrentNormalPosition();
        }
    }

    boolean itToday(Date date) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date);
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
}
