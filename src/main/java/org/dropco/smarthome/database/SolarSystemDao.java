package org.dropco.smarthome.database;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.querydsl.SolarPosition;
import org.dropco.smarthome.solar.SolarPanelPosition;
import org.dropco.smarthome.solar.SolarPanelStepRecord;
import org.dropco.smarthome.solar.SolarSystemRefCode;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;

import static org.dropco.smarthome.database.querydsl.SolarPanelSchedule.SOLAR_SCHEDULE;
import static org.dropco.smarthome.database.querydsl.SolarPosition.SOLAR_POSITION;

public class SolarSystemDao {
    private SettingsDao settingsDao;
    private static final Template toDate = TemplateFactory.DEFAULT.create("STR_TO_DATE(CONCAT({0},'-',{1},'-',{2},' ',{3},':',{4},':',{5}), '%Y-%c-%e %k:%i:%s')");

    public SolarSystemDao(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public SolarPanelPosition getLastKnownPosition() {
        SolarPanelPosition solarPanelPosition = getPositionById(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD));
        return solarPanelPosition;
    }

    SolarPanelPosition getPositionById(long id) {
        Tuple tuple = new MySQLQuery<SolarPosition>(getConnection()).select(SOLAR_POSITION.all()).from(SOLAR_POSITION).where(SOLAR_POSITION.id.eq(id)).fetchFirst();
        SolarPanelPosition solarPanelPosition = new SolarPanelPosition();
        solarPanelPosition.setVerticalPositionInSeconds(tuple.get(SOLAR_POSITION.verticalPosition));
        solarPanelPosition.setHorizontalPositionInSeconds(tuple.get(SOLAR_POSITION.horizontalPosition));
        return solarPanelPosition;
    }

    public SolarPanelPosition getStrongWindPosition() {
        SolarPanelPosition solarPanelPosition = getPositionById(settingsDao.getLong(SolarSystemRefCode.STRONG_WIND_POSITION_REF_CD));
        return solarPanelPosition;
    }

    public SolarPanelPosition getOverheatedPosition() {
        SolarPanelPosition solarPanelPosition = getPositionById(settingsDao.getLong(SolarSystemRefCode.OVERHEAT_POSITION_REF_CD));
        return solarPanelPosition;
    }

    public SolarPanelStepRecord getNextRecord(Calendar calendar, SolarPanelPosition currentPosition) {
        Tuple tuple = getNextPosition(calendar, currentPosition);
        SolarPanelStepRecord record = new SolarPanelStepRecord();
        record.setHour(tuple.get(SOLAR_SCHEDULE.hour));
        record.setMonth(tuple.get(SOLAR_SCHEDULE.month));
        record.setMinute(tuple.get(SOLAR_SCHEDULE.minute));
        record.setDay(tuple.get(SOLAR_SCHEDULE.day));
        SolarPanelPosition position = new SolarPanelPosition();
        position.setHorizontalPositionInSeconds(tuple.get(SOLAR_POSITION.horizontalPosition));
        position.setVerticalPositionInSeconds(tuple.get(SOLAR_POSITION.verticalPosition));
        record.setPanelPosition(position);
        return record;
    }

    Tuple getNextPosition(Calendar calendar, SolarPanelPosition currentPosition) {
        Expression<?>[] list = new Expression[]{SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day};
        BooleanExpression whereCond = Expressions.dateTemplate(Date.class,toDate,calendar.get(Calendar.YEAR),SOLAR_SCHEDULE.month,SOLAR_SCHEDULE.day,SOLAR_SCHEDULE.hour,SOLAR_SCHEDULE.minute,0).goe(calendar.getTime());
        whereCond = whereCond.and(SOLAR_POSITION.horizontalPosition.ne(currentPosition.getHorizontalPositionInSeconds())).and(SOLAR_POSITION.verticalPosition.ne(currentPosition.getVerticalPositionInSeconds()));
        return new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id)).where(whereCond).fetchFirst();
    }


    public void updateLastKnownPosition(SolarPanelPosition currentPosition) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, SOLAR_POSITION)
                .set(SOLAR_POSITION.verticalPosition, currentPosition.getVerticalPositionInSeconds())
                .set(SOLAR_POSITION.horizontalPosition, currentPosition.getHorizontalPositionInSeconds())
                .where(SOLAR_POSITION.id.eq(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD)))
                .execute();
    }

    private Connection getConnection() {
        return DBConnection.getConnection();
    }
}
