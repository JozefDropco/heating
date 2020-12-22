package org.dropco.smarthome.solar;

import com.google.common.collect.FluentIterable;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Template;
import com.querydsl.core.types.TemplateFactory;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.dml.SQLUpdateClause;
import com.querydsl.sql.mysql.MySQLQuery;
import org.dropco.smarthome.database.DBConnection;
import org.dropco.smarthome.database.SettingsDao;
import org.dropco.smarthome.database.querydsl.SolarPosition;

import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.dropco.smarthome.database.querydsl.SolarPanelSchedule.SOLAR_SCHEDULE;
import static org.dropco.smarthome.database.querydsl.SolarPosition.SOLAR_POSITION;

public class SolarSystemDao {
    protected static final String SOLAR_PANEL_DELAY = "SOLAR_PANEL_DELAY";
    private SettingsDao settingsDao;
    private static final Template toDate = TemplateFactory.DEFAULT.create("STR_TO_DATE(CONCAT({0},'-',{1},'-',{2},' ',{3},':',{4},':',{5}), '%Y-%c-%e %k:%i:%s')");

    public SolarSystemDao(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public SolarPanelPosition getLastKnownPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD));
    }

    SolarPanelPosition getPositionById(long id) {
        Tuple tuple = new MySQLQuery<SolarPosition>(getConnection()).select(SOLAR_POSITION.all()).from(SOLAR_POSITION).where(SOLAR_POSITION.id.eq(id)).fetchFirst();
        SolarPanelPosition solarPanelPosition = new SolarPanelPosition();
        solarPanelPosition.setVerticalPositionInSeconds(tuple.get(SOLAR_POSITION.verticalPosition));
        solarPanelPosition.setHorizontalPositionInSeconds(tuple.get(SOLAR_POSITION.horizontalPosition));
        return solarPanelPosition;
    }

    public SolarPanelPosition getStrongWindPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.STRONG_WIND_POSITION_REF_CD));
    }

    public SolarPanelPosition getOverheatedPosition() {
        return getPositionById(settingsDao.getLong(SolarSystemRefCode.OVERHEAT_POSITION_REF_CD));
    }

    public void updateLastKnownPosition(SolarPanelPosition currentPosition) {
        new SQLUpdateClause(getConnection(), SQLTemplates.DEFAULT, SOLAR_POSITION)
                .set(SOLAR_POSITION.verticalPosition, currentPosition.getVerticalPositionInSeconds())
                .set(SOLAR_POSITION.horizontalPosition, currentPosition.getHorizontalPositionInSeconds())
                .where(SOLAR_POSITION.id.eq(settingsDao.getLong(SolarSystemRefCode.LAST_KNOWN_POSITION_REF_CD)))
                .execute();
    }


    public List<SolarPanelStepRecord> getTodayRecords(Calendar cal) {
        Expression<?>[] list = new Expression[]{SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day};
        DateTemplate<Date> dateTemplate = Expressions.dateTemplate(Date.class, toDate, cal.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        Date currentTime = cal.getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        DateTemplate<Date> dateTemplateNextDay = Expressions.dateTemplate(Date.class, toDate, calendar.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        BooleanExpression whereCond = dateTemplate.goe(currentTime).and(dateTemplateNextDay.loe(calendar.getTime()));
        List<Tuple> lst = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id)).where(whereCond).orderBy(dateTemplate.asc()).
                fetch();
        return FluentIterable.from(lst).transform(tuple -> toRecord(tuple)).toList();
    }

    private SolarPanelStepRecord toRecord(Tuple tuple) {
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

    private Connection getConnection() {
        return DBConnection.getConnection();
    }

    public SolarPanelStepRecord getRecentRecord(Calendar instance) {
        Expression<?>[] list = new Expression[]{SOLAR_POSITION.horizontalPosition, SOLAR_POSITION.verticalPosition, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day};
        DateTemplate<Date> dateTemplate = Expressions.dateTemplate(Date.class, toDate, instance.get(Calendar.YEAR), SOLAR_SCHEDULE.month, SOLAR_SCHEDULE.day, SOLAR_SCHEDULE.hour, SOLAR_SCHEDULE.minute, 0);
        BooleanExpression where = dateTemplate.loe(instance.getTime());
        Tuple tuple = new MySQLQuery<SolarPosition>(getConnection()).select(list).from(SOLAR_SCHEDULE).join(SOLAR_POSITION).on(SOLAR_SCHEDULE.position.eq(SOLAR_POSITION.id)).where(where)
                .orderBy(dateTemplate.desc()).
                        fetchFirst();
        return toRecord(tuple);
    }

    public long getDelay() {
        return settingsDao.getLong(SOLAR_PANEL_DELAY);
    }
}
