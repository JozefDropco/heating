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
import org.dropco.smarthome.heating.solar.dto.AbsolutePosition;
import org.dropco.smarthome.heating.solar.dto.DeltaPosition;
import org.dropco.smarthome.heating.solar.dto.SolarPanelStep;
import org.dropco.smarthome.heating.solar.dto.SolarSchedule;
import org.dropco.smarthome.heating.solar.SolarSystemRefCode;

import java.sql.Connection;
import java.util.Calendar;
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
        sunRise.setPosition(new AbsolutePosition(sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosHor), sTuple.get(SOLAR_SCHEDULE.sunRiseAbsPosVert)));
        sunRise.setIgnoreDayLight(true);
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
        return new DeltaPosition(0,month.getVerticalTickCountForStep() * -2);
    }


}
